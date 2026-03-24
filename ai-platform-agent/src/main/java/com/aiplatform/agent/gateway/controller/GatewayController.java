package com.aiplatform.agent.gateway.controller;

import com.aiplatform.agent.gateway.dto.ChatCompletionRequest;
import com.aiplatform.agent.gateway.service.GatewayService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * AI 网关控制器。
 *
 * <p>提供同步和流式两个聊天补全端点，委托 {@link GatewayService} 完成
 * 认证、配额校验、路由、上下文增强、调用、配额扣减和用量记录的完整流程。</p>
 */
@RestController
@RequestMapping("/api/gateway")
public class GatewayController {

    private final GatewayService gatewayService;

    public GatewayController(GatewayService gatewayService) {
        this.gatewayService = gatewayService;
    }

    /**
     * 聊天补全接口（同步模式）。
     */
    @PostMapping("/chat/completions")
    public ResponseEntity<String> chatCompletions(
            @RequestHeader("Authorization") String authorization,
            @RequestHeader(value = "X-Project-Id", required = false) Long projectId,
            @RequestBody ChatCompletionRequest request) {
        String response = gatewayService.chatCompletion(authorization, projectId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 聊天补全接口（SSE 流式模式）。
     */
    @PostMapping(value = "/chat/completions/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> chatCompletionsStream(
            @RequestHeader("Authorization") String authorization,
            @RequestHeader(value = "X-Project-Id", required = false) Long projectId,
            @RequestBody ChatCompletionRequest request) {
        return gatewayService.chatCompletionStream(authorization, projectId, request)
                .map(data -> ServerSentEvent.<String>builder().data(data).build());
    }

}
