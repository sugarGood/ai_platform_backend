package com.aiplatform.agent.gateway.dto;

import java.math.BigDecimal;

/**
 * 用量落库命令。
 *
 * <p>用于承载一次网关请求的完整用量写入参数，避免超长方法参数列表。</p>
 */
public record UsageRecordCommand(
        Long credentialId,
        Long userId,
        Long projectId,
        Long providerId,
        Long providerApiKeyId,
        Long modelId,
        String requestMode,
        String requestId,
        long latencyMs,
        long inputTokens,
        long outputTokens,
        long totalTokens,
        BigDecimal inputPricePer1m,
        BigDecimal outputPricePer1m,
        String quotaCheckResult
) {
}
