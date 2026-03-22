package com.aiplatform.agent.gateway.service;

import com.aiplatform.agent.gateway.dto.ChatCompletionRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.Base64;

/**
 * 上游 AI 供应商调用客户端。
 *
 * <p>负责将聊天补全请求转发至上游 AI 供应商的 API 端点，支持同步调用和流式响应两种模式。
 * 调用前会对加密存储的 API Key 进行解密，并携带在请求头中完成鉴权。</p>
 */
@Service
public class ChatCompletionUpstreamClient {

    /** WebClient 构建器，用于创建 HTTP 客户端实例 */
    private final WebClient.Builder webClientBuilder;

    /**
     * 构造上游调用客户端。
     *
     * @param webClientBuilder Spring 注入的 WebClient 构建器
     */
    public ChatCompletionUpstreamClient(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    /**
     * 以流式方式调用上游聊天补全接口。
     *
     * @param baseUrl         供应商 API 基础 URL
     * @param apiKeyEncrypted 加密存储的 API Key
     * @param request         聊天补全请求参数
     * @return 流式响应数据
     */
    public Flux<String> streamChatCompletion(String baseUrl, String apiKeyEncrypted,
                                              ChatCompletionRequest request) {
        String apiKey = decryptApiKey(apiKeyEncrypted);

        // 上游要求 stream=true 才会返回 SSE 分块响应
        ChatCompletionRequest streamRequest = new ChatCompletionRequest(
                request.model(), request.messages(), request.temperature(), request.maxTokens(), Boolean.TRUE);

        WebClient client = webClientBuilder.baseUrl(baseUrl).build();

        return client.post()
                .uri("/v1/chat/completions")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(streamRequest)
                .retrieve()
                .bodyToFlux(String.class);
    }

    /**
     * 以同步方式调用上游聊天补全接口。
     *
     * @param baseUrl         供应商 API 基础 URL
     * @param apiKeyEncrypted 加密存储的 API Key
     * @param request         聊天补全请求参数
     * @return 上游供应商返回的完整 JSON 响应
     */
    public String callChatCompletion(String baseUrl, String apiKeyEncrypted,
                                      ChatCompletionRequest request) {
        String apiKey = decryptApiKey(apiKeyEncrypted);

        WebClient client = webClientBuilder.baseUrl(baseUrl).build();

        return client.post()
                .uri("/v1/chat/completions")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    /**
     * 解密 API Key。
     *
     * <p>当前实现使用 Base64 解码，生产环境应替换为更安全的加密方案。</p>
     *
     * @param encrypted 加密后的 API Key 字符串
     * @return 解密后的 API Key 明文
     */
    private String decryptApiKey(String encrypted) {
        return new String(Base64.getDecoder().decode(encrypted));
    }
}
