package com.aiplatform.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * 创建 AI 模型的请求参数。
 *
 * @param providerId      所属供应商ID（必填）
 * @param code            模型标识编码（必填），如 claude-opus-4-6
 * @param name            模型展示名称（必填）
 * @param modelFamily     模型系列，如 Claude、GPT
 * @param contextWindow   上下文窗口大小（Token 数）
 * @param inputPricePer1m 输入价格（美元/百万 Token）
 * @param outputPricePer1m 输出价格（美元/百万 Token）
 */
public record CreateAiModelRequest(
        @NotNull(message = "Provider ID must not be null")
        Long providerId,
        @NotBlank(message = "Model code must not be blank")
        String code,
        @NotBlank(message = "Model name must not be blank")
        String name,
        String modelFamily,
        Integer contextWindow,
        BigDecimal inputPricePer1m,
        BigDecimal outputPricePer1m
) {
}
