package com.aiplatform.agent.gateway.service;

import com.aiplatform.agent.gateway.dto.ChatCompletionRequest;
import com.aiplatform.agent.gateway.dto.ChatCompletionResponse;
import com.aiplatform.agent.gateway.dto.UsageRecordCommand;
import com.aiplatform.agent.gateway.entity.AiModelRef;
import com.aiplatform.agent.gateway.entity.PlatformCredentialRef;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 网关核心服务，编排认证、配额校验、路由、上下文增强和用量记录。
 */
@Service
public class GatewayService {

    private static final Logger log = LoggerFactory.getLogger(GatewayService.class);

    private final CredentialAuthService credentialAuthService;
    private final QuotaCheckService quotaCheckService;
    private final GatewayRoutingService routingService;
    private final ProjectContextEnrichmentService enrichmentService;
    private final ProjectAccessValidationService projectAccessValidationService;
    private final ChatCompletionUpstreamClient upstreamClient;
    private final QuotaDeductionService quotaDeductionService;
    private final UsageRecordingService usageRecordingService;
    private final ObjectMapper objectMapper;

    public GatewayService(CredentialAuthService credentialAuthService,
                          QuotaCheckService quotaCheckService,
                          GatewayRoutingService routingService,
                          ProjectContextEnrichmentService enrichmentService,
                          ProjectAccessValidationService projectAccessValidationService,
                          ChatCompletionUpstreamClient upstreamClient,
                          QuotaDeductionService quotaDeductionService,
                          UsageRecordingService usageRecordingService,
                          ObjectMapper objectMapper) {
        this.credentialAuthService = credentialAuthService;
        this.quotaCheckService = quotaCheckService;
        this.routingService = routingService;
        this.enrichmentService = enrichmentService;
        this.projectAccessValidationService = projectAccessValidationService;
        this.upstreamClient = upstreamClient;
        this.quotaDeductionService = quotaDeductionService;
        this.usageRecordingService = usageRecordingService;
        this.objectMapper = objectMapper;
    }

    /**
     * 同步聊天补全。
     */
    public String chatCompletion(String authorization, Long headerProjectId, ChatCompletionRequest request) {
        GatewayExecutionContext context = prepareExecutionContext(authorization, headerProjectId, request);

        long startMs = System.currentTimeMillis();
        String response = upstreamClient.callChatCompletion(
                context.route().provider().getBaseUrl(),
                context.route().apiKey().getApiKeyEncrypted(),
                context.enrichedRequest()
        );
        long latencyMs = System.currentTimeMillis() - startMs;

        UsageSnapshot usage = parseUsage(response);
        finalizeUsage(context, "CHAT", latencyMs, usage);
        return response;
    }

    /**
     * 流式聊天补全。
     */
    public Flux<String> chatCompletionStream(String authorization, Long headerProjectId, ChatCompletionRequest request) {
        GatewayExecutionContext context = prepareExecutionContext(authorization, headerProjectId, request);
        long startMs = System.currentTimeMillis();

        AtomicLong inputTokens = new AtomicLong();
        AtomicLong outputTokens = new AtomicLong();
        AtomicLong totalTokens = new AtomicLong();

        return upstreamClient.streamChatCompletion(
                context.route().provider().getBaseUrl(),
                context.route().apiKey().getApiKeyEncrypted(),
                context.enrichedRequest()
        ).doOnNext(chunk -> updateUsageFromChunk(chunk, inputTokens, outputTokens, totalTokens))
                .doOnComplete(() -> finalizeUsage(
                        context,
                        "STREAM",
                        System.currentTimeMillis() - startMs,
                        new UsageSnapshot(inputTokens.get(), outputTokens.get(), totalTokens.get())));
    }

    private GatewayExecutionContext prepareExecutionContext(
            String authorization,
            Long headerProjectId,
            ChatCompletionRequest request) {
        PlatformCredentialRef credential = credentialAuthService.authenticate(authorization);
        Long projectId = resolveProjectId(headerProjectId, credential);
        projectAccessValidationService.validate(credential, projectId);
        QuotaCheckService.QuotaCheckResult quotaCheckResult = quotaCheckService.check(credential, projectId);
        GatewayRoutingService.RoutingResult route = routingService.resolve(request.model());
        ChatCompletionRequest enrichedRequest = enrichmentService.enrich(projectId, credential.getUserId(), request);
        return new GatewayExecutionContext(credential, projectId, route, enrichedRequest, quotaCheckResult);
    }

    private Long resolveProjectId(Long headerProjectId, PlatformCredentialRef credential) {
        if (headerProjectId != null) {
            return headerProjectId;
        }
        return credential.getBoundProjectId();
    }

    private UsageSnapshot parseUsage(String response) {
        try {
            ChatCompletionResponse parsed = objectMapper.readValue(response, ChatCompletionResponse.class);
            if (parsed.usage() == null) {
                return UsageSnapshot.empty();
            }
            return new UsageSnapshot(
                    parsed.usage().promptTokens(),
                    parsed.usage().completionTokens(),
                    parsed.usage().totalTokens());
        } catch (JsonProcessingException e) {
            log.warn("无法解析上游响应中的 usage 字段，Token 用量将记为 0", e);
            return UsageSnapshot.empty();
        }
    }

    private void updateUsageFromChunk(String chunk,
                                      AtomicLong inputTokens,
                                      AtomicLong outputTokens,
                                      AtomicLong totalTokens) {
        if (chunk == null || !chunk.contains("\"usage\"")) {
            return;
        }

        try {
            String json = chunk.startsWith("data:") ? chunk.substring(5).trim() : chunk;
            if ("[DONE]".equals(json)) {
                return;
            }
            ChatCompletionResponse parsed = objectMapper.readValue(json, ChatCompletionResponse.class);
            if (parsed.usage() == null || parsed.usage().totalTokens() <= 0) {
                return;
            }
            inputTokens.set(parsed.usage().promptTokens());
            outputTokens.set(parsed.usage().completionTokens());
            totalTokens.set(parsed.usage().totalTokens());
        } catch (JsonProcessingException e) {
            log.debug("流式 chunk 解析 usage 失败，忽略该片段: {}", e.getMessage());
        }
    }

    private void finalizeUsage(GatewayExecutionContext context,
                               String requestMode,
                               long latencyMs,
                               UsageSnapshot usage) {
        if (usage.totalTokens() > 0) {
            quotaDeductionService.deduct(
                    context.credential().getId(),
                    context.projectId(),
                    usage.totalTokens());
        }

        AiModelRef model = context.route().model();
        UsageRecordCommand recordCommand = new UsageRecordCommand(
                context.credential().getId(),
                context.credential().getUserId(),
                context.projectId(),
                context.route().provider().getId(),
                context.route().apiKey().getId(),
                model.getId(),
                requestMode,
                context.requestId(),
                latencyMs,
                usage.inputTokens(),
                usage.outputTokens(),
                usage.totalTokens(),
                model.getInputPricePer1m(),
                model.getOutputPricePer1m(),
                context.quotaCheckResult().code().name()
        );
        usageRecordingService.record(recordCommand);
    }

    private record GatewayExecutionContext(
            PlatformCredentialRef credential,
            Long projectId,
            GatewayRoutingService.RoutingResult route,
            ChatCompletionRequest enrichedRequest,
            QuotaCheckService.QuotaCheckResult quotaCheckResult
    ) {
        String requestId() {
            return "req_" + UUID.randomUUID().toString().replace("-", "");
        }
    }

    private record UsageSnapshot(long inputTokens, long outputTokens, long totalTokens) {

        private static UsageSnapshot empty() {
            return new UsageSnapshot(0, 0, 0);
        }
    }
}
