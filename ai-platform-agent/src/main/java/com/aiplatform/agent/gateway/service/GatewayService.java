package com.aiplatform.agent.gateway.service;

import com.aiplatform.agent.gateway.dto.ChatCompletionRequest;
import com.aiplatform.agent.gateway.dto.ChatCompletionResponse;
import com.aiplatform.agent.gateway.entity.AiModelRef;
import com.aiplatform.agent.gateway.entity.PlatformCredentialRef;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 网关核心服务。
 *
 * <p>编排 AI 网关的完整处理流程：
 * 凭证认证 → 配额校验 → 路由解析 → 上下文增强 → 上游调用 → 解析响应 → 配额扣减 → 用量记录。</p>
 */
@Service
public class GatewayService {

    private static final Logger log = LoggerFactory.getLogger(GatewayService.class);

    private final CredentialAuthService credentialAuthService;
    private final QuotaCheckService quotaCheckService;
    private final GatewayRoutingService routingService;
    private final ProjectContextEnrichmentService enrichmentService;
    private final ChatCompletionUpstreamClient upstreamClient;
    private final QuotaDeductionService quotaDeductionService;
    private final UsageRecordingService usageRecordingService;
    private final ObjectMapper objectMapper;

    public GatewayService(CredentialAuthService credentialAuthService,
                          QuotaCheckService quotaCheckService,
                          GatewayRoutingService routingService,
                          ProjectContextEnrichmentService enrichmentService,
                          ChatCompletionUpstreamClient upstreamClient,
                          QuotaDeductionService quotaDeductionService,
                          UsageRecordingService usageRecordingService,
                          ObjectMapper objectMapper) {
        this.credentialAuthService = credentialAuthService;
        this.quotaCheckService = quotaCheckService;
        this.routingService = routingService;
        this.enrichmentService = enrichmentService;
        this.upstreamClient = upstreamClient;
        this.quotaDeductionService = quotaDeductionService;
        this.usageRecordingService = usageRecordingService;
        this.objectMapper = objectMapper;
    }

    /**
     * 解析最终生效的项目 ID。
     *
     * <p>优先级：
     * <ol>
     *   <li>请求头 {@code X-Project-Id} 显式传入（兼容能自定义请求头的调用方）</li>
     *   <li>凭证绑定的当前工作项目 {@code boundProjectId}（员工在个人页面切换项目后生效，
     *       Cursor / Claude Code / Codex 等工具无需任何额外配置）</li>
     *   <li>{@code null}，不关联项目，仅做纯模型调用</li>
     * </ol>
     * </p>
     *
     * @param headerProjectId 请求头传入的项目 ID，可为 null
     * @param credential      认证通过的平台凭证
     * @return 最终生效的项目 ID
     */
    private Long resolveProjectId(Long headerProjectId, PlatformCredentialRef credential) {
        if (headerProjectId != null) {
            return headerProjectId;
        }
        return credential.getBoundProjectId();
    }

    /**
     * 执行聊天补全请求的完整流程（同步模式）。
     *
     * @param authorization   请求头中的 Authorization 值
     * @param headerProjectId 请求头 X-Project-Id 中的项目 ID（可为 null，自动回退到凭证绑定的工作项目）
     * @param request         聊天补全请求参数
     * @return 上游供应商返回的原始 JSON 响应
     */
    public String chatCompletion(String authorization, Long headerProjectId, ChatCompletionRequest request) {
        // 1. 凭证认证
        PlatformCredentialRef credential = credentialAuthService.authenticate(authorization);
        // 项目 ID 解析：优先请求头 > 凭证绑定的工作项目 > null
        Long projectId = resolveProjectId(headerProjectId, credential);
        // 2. 配额校验（双池）
        quotaCheckService.check(credential, projectId);
        // 3. 路由解析
        GatewayRoutingService.RoutingResult route = routingService.resolve(request.model());

        // 4. 上下文增强（含 RAG 检索）
        ChatCompletionRequest enrichedRequest = enrichmentService.enrich(projectId, credential.getUserId(), request);

        // 5. 上游调用
        long startMs = System.currentTimeMillis();
        String response = upstreamClient.callChatCompletion(
                route.provider().getBaseUrl(),
                route.apiKey().getApiKeyEncrypted(),
                enrichedRequest
        );
        long latencyMs = System.currentTimeMillis() - startMs;

        // 6. 解析响应中的 Token 用量
        long inputTokens = 0;
        long outputTokens = 0;
        long totalTokens = 0;
        try {
            ChatCompletionResponse parsed = objectMapper.readValue(response, ChatCompletionResponse.class);
            if (parsed.usage() != null) {
                inputTokens = parsed.usage().promptTokens();
                outputTokens = parsed.usage().completionTokens();
                totalTokens = parsed.usage().totalTokens();
            }
        } catch (JsonProcessingException e) {
            log.warn("无法解析上游响应中的 usage 字段，Token 用量将记为 0", e);
        }

        // 7. 配额扣减（双池）
        quotaDeductionService.deduct(credential.getId(), projectId, totalTokens);

        // 8. 用量记录
        AiModelRef model = route.model();
        usageRecordingService.record(
                credential.getId(),
                credential.getUserId(),
                projectId,
                route.provider().getId(),
                route.apiKey().getId(),
                model.getId(),
                request.model(),
                "CHAT",
                latencyMs,
                inputTokens,
                outputTokens,
                totalTokens,
                model.getInputPricePer1m(),
                model.getOutputPricePer1m()
        );

        return response;
    }

    /**
     * 执行聊天补全请求的完整流程（流式模式）。
     *
     * <p>返回 SSE 流式响应。Token 用量在流式模式下无法实时统计，
     * 配额扣减依赖上游最后一个 chunk 中的 usage 字段（如果有）。</p>
     *
     * @param authorization   请求头中的 Authorization 值
     * @param headerProjectId 请求头 X-Project-Id 中的项目 ID（可为 null，自动回退到凭证绑定的工作项目）
     * @param request         聊天补全请求参数
     * @return 流式响应数据
     */
    public Flux<String> chatCompletionStream(String authorization, Long headerProjectId, ChatCompletionRequest request) {
        // 1. 凭证认证
        PlatformCredentialRef credential = credentialAuthService.authenticate(authorization);
        // 项目 ID 解析：优先请求头 > 凭证绑定的工作项目 > null
        Long projectId = resolveProjectId(headerProjectId, credential);

        // 2. 配额校验（双池）
        quotaCheckService.check(credential, projectId);

        // 3. 路由解析
        GatewayRoutingService.RoutingResult route = routingService.resolve(request.model());

        // 4. 上下文增强（含 RAG 检索）
        ChatCompletionRequest enrichedRequest = enrichmentService.enrich(projectId, credential.getUserId(), request);

        // 5. 流式调用上游
        long startMs = System.currentTimeMillis();

        // 用原子变量跨 lambda 累积 token 用量（从最后一个含 usage 的 SSE chunk 解析）
        AtomicLong inputTokensHolder  = new AtomicLong(0);
        AtomicLong outputTokensHolder = new AtomicLong(0);
        AtomicLong totalTokensHolder  = new AtomicLong(0);

        return upstreamClient.streamChatCompletion(
                route.provider().getBaseUrl(),
                route.apiKey().getApiKeyEncrypted(),
                enrichedRequest
        ).doOnNext(chunk -> {
            // 部分上游（如 OpenAI stream_options.include_usage=true）会在最后一个 chunk 携带 usage
            if (chunk != null && chunk.contains("\"usage\"")) {
                try {
                    // SSE chunk 格式: "data: {...}" 或直接 JSON
                    String json = chunk.startsWith("data:") ? chunk.substring(5).trim() : chunk;
                    if (!"[DONE]".equals(json)) {
                        ChatCompletionResponse parsed = objectMapper.readValue(json, ChatCompletionResponse.class);
                        if (parsed.usage() != null && parsed.usage().totalTokens() > 0) {
                            inputTokensHolder.set(parsed.usage().promptTokens());
                            outputTokensHolder.set(parsed.usage().completionTokens());
                            totalTokensHolder.set(parsed.usage().totalTokens());
                        }
                    }
                } catch (JsonProcessingException e) {
                    log.debug("流式 chunk 解析 usage 失败（非最终 chunk），忽略: {}", e.getMessage());
                }
            }
        }).doOnComplete(() -> {
            long latencyMs = System.currentTimeMillis() - startMs;
            long inputTokens  = inputTokensHolder.get();
            long outputTokens = outputTokensHolder.get();
            long totalTokens  = totalTokensHolder.get();

            // 配额扣减（双池）
            if (totalTokens > 0) {
                quotaDeductionService.deduct(credential.getId(), projectId, totalTokens);
            }

            // 用量记录
            AiModelRef model = route.model();
            usageRecordingService.record(
                    credential.getId(),
                    credential.getUserId(),
                    projectId,
                    route.provider().getId(),
                    route.apiKey().getId(),
                    model.getId(),
                    request.model(),
                    "STREAM",
                    latencyMs,
                    inputTokens,
                    outputTokens,
                    totalTokens,
                    model.getInputPricePer1m(),
                    model.getOutputPricePer1m()
            );
        });
    }
}
