package com.aiplatform.agent.gateway.controller;

import com.aiplatform.agent.gateway.dto.ChatCompletionRequest;
import com.aiplatform.agent.gateway.service.CredentialAuthService;
import com.aiplatform.agent.gateway.service.GatewayRoutingService;
import com.aiplatform.agent.gateway.service.GatewayService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * AI 网关控制器。
 *
 * <p>提供 {@code /api/gateway/chat/completions} 端点，接收外部聊天补全请求，
 * 委托 {@link GatewayService} 完成认证、路由、调用和用量记录的完整流程。
 * 同时处理各类业务异常并返回对应的 HTTP 状态码。</p>
 */
@RestController
@RequestMapping("/api/gateway")
public class GatewayController {

    /** 网关核心服务 */
    private final GatewayService gatewayService;

    /**
     * 构造网关控制器。
     *
     * @param gatewayService 网关核心服务
     */
    public GatewayController(GatewayService gatewayService) {
        this.gatewayService = gatewayService;
    }

    /**
     * 聊天补全接口。
     *
     * <p>接收客户端的聊天补全请求，经网关认证和路由后转发至上游 AI 供应商，
     * 并将供应商响应原样返回给调用方。</p>
     *
     * @param authorization 请求头中的 Authorization（Bearer Token）
     * @param request       聊天补全请求体
     * @return 上游供应商返回的响应
     */
    @PostMapping("/chat/completions")
    public ResponseEntity<String> chatCompletions(
            @RequestHeader("Authorization") String authorization,
            @RequestBody ChatCompletionRequest request) {
        String response = gatewayService.chatCompletion(authorization, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 处理凭证无效异常，返回 401 Unauthorized。
     *
     * @param ex 凭证无效异常
     * @return 包含错误信息的响应
     */
    @ExceptionHandler(CredentialAuthService.InvalidCredentialException.class)
    public ResponseEntity<Map<String, String>> handleInvalidCredential(CredentialAuthService.InvalidCredentialException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", ex.getMessage()));
    }

    /**
     * 处理模型未找到异常，返回 404 Not Found。
     *
     * @param ex 模型未找到异常
     * @return 包含错误信息的响应
     */
    @ExceptionHandler(GatewayRoutingService.ModelNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleModelNotFound(GatewayRoutingService.ModelNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", ex.getMessage()));
    }

    /**
     * 处理供应商不可用异常，返回 503 Service Unavailable。
     *
     * @param ex 供应商不可用异常
     * @return 包含错误信息的响应
     */
    @ExceptionHandler(GatewayRoutingService.ProviderNotAvailableException.class)
    public ResponseEntity<Map<String, String>> handleProviderNotAvailable(GatewayRoutingService.ProviderNotAvailableException ex) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("error", ex.getMessage()));
    }
}
