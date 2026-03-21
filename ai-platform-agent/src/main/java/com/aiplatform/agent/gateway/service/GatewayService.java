package com.aiplatform.agent.gateway.service;

import com.aiplatform.agent.gateway.dto.ChatCompletionRequest;
import com.aiplatform.agent.gateway.entity.PlatformCredentialRef;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 网关核心服务。
 *
 * <p>编排 AI 网关的完整处理流程：凭证认证 → 路由解析 → 上游调用 → 用量记录。
 * 作为网关业务逻辑的核心协调者，串联各个子服务完成一次完整的 AI 请求代理。</p>
 */
@Service
public class GatewayService {

    /** 凭证认证服务 */
    private final CredentialAuthService credentialAuthService;

    /** 网关路由服务 */
    private final GatewayRoutingService routingService;

    /** 上游供应商调用客户端 */
    private final ChatCompletionUpstreamClient upstreamClient;

    /** 用量记录服务 */
    private final UsageRecordingService usageRecordingService;

    /**
     * 构造网关核心服务。
     *
     * @param credentialAuthService 凭证认证服务
     * @param routingService        网关路由服务
     * @param upstreamClient        上游供应商调用客户端
     * @param usageRecordingService 用量记录服务
     */
    public GatewayService(CredentialAuthService credentialAuthService,
                          GatewayRoutingService routingService,
                          ChatCompletionUpstreamClient upstreamClient,
                          UsageRecordingService usageRecordingService) {
        this.credentialAuthService = credentialAuthService;
        this.routingService = routingService;
        this.upstreamClient = upstreamClient;
        this.usageRecordingService = usageRecordingService;
    }

    /**
     * 执行聊天补全请求的完整流程。
     *
     * <p>处理步骤：
     * <ol>
     *   <li>验证调用方凭证</li>
     *   <li>根据模型编码解析路由（供应商 + API 密钥）</li>
     *   <li>调用上游供应商 API 并计算延迟</li>
     *   <li>记录本次调用的用量信息</li>
     * </ol>
     * </p>
     *
     * @param authorization 请求头中的 Authorization 值
     * @param request       聊天补全请求参数
     * @return 上游供应商返回的原始 JSON 响应
     */
    public String chatCompletion(String authorization, ChatCompletionRequest request) {
        PlatformCredentialRef credential = credentialAuthService.authenticate(authorization);

        GatewayRoutingService.RoutingResult route = routingService.resolve(request.model());

        long startMs = System.currentTimeMillis();
        String response = upstreamClient.callChatCompletion(
                route.provider().getBaseUrl(),
                route.apiKey().getApiKeyEncrypted(),
                request
        );
        long latencyMs = System.currentTimeMillis() - startMs;

        usageRecordingService.record(
                credential.getId(),
                credential.getUserId(),
                route.provider().getId(),
                route.apiKey().getId(),
                route.model().getId(),
                request.model(),
                latencyMs
        );

        return response;
    }
}
