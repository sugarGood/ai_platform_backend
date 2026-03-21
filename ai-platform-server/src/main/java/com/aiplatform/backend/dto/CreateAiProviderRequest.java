package com.aiplatform.backend.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 创建 AI 供应商的请求参数。
 *
 * @param code         供应商编码（必填），如 anthropic、openai
 * @param name         供应商名称（必填）
 * @param providerType 供应商类型（必填），如 OPENAI、ANTHROPIC、GOOGLE
 * @param baseUrl      API 基础URL
 */
public record CreateAiProviderRequest(
        @NotBlank(message = "Provider code must not be blank")
        String code,
        @NotBlank(message = "Provider name must not be blank")
        String name,
        @NotBlank(message = "Provider type must not be blank")
        String providerType,
        String baseUrl
) {
}
