package com.aiplatform.agent.gateway.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 上游 AI 供应商聊天补全响应。
 *
 * <p>仅解析网关关心的字段（usage），其余字段透传给调用方。
 * 兼容 OpenAI 及其兼容格式。</p>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ChatCompletionResponse(
        String id,
        String model,
        Usage usage
) {

    /**
     * Token 用量信息。
     *
     * @param promptTokens     输入 Token 数
     * @param completionTokens 输出 Token 数
     * @param totalTokens      总 Token 数
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Usage(
            @JsonProperty("prompt_tokens") long promptTokens,
            @JsonProperty("completion_tokens") long completionTokens,
            @JsonProperty("total_tokens") long totalTokens
    ) {
    }
}
