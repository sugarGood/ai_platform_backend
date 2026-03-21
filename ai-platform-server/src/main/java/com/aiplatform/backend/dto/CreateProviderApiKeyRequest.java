package com.aiplatform.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 创建上游 API 密钥的请求参数。
 * <p>{@code apiKey} 为原始明文密钥，服务端存储时会进行加密处理。</p>
 *
 * @param providerId        所属供应商ID（必填）
 * @param label             密钥标识名称（必填），便于管理区分
 * @param apiKey            API 密钥原始明文（必填）
 * @param modelsAllowed     可用模型列表，JSON 格式
 * @param monthlyQuotaTokens 月度 Token 配额
 * @param rateLimitRpm      每分钟请求数限制（RPM）
 * @param rateLimitTpm      每分钟 Token 数限制（TPM）
 * @param proxyEndpoint     代理端点URL
 */
public record CreateProviderApiKeyRequest(
        @NotNull(message = "Provider ID must not be null")
        Long providerId,
        @NotBlank(message = "Label must not be blank")
        String label,
        @NotBlank(message = "API key must not be blank")
        String apiKey,
        String modelsAllowed,
        Long monthlyQuotaTokens,
        Integer rateLimitRpm,
        Integer rateLimitTpm,
        String proxyEndpoint
) {
}
