package com.aiplatform.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 我的用量事件明细行。
 */
public record MyUsageEventResponse(
        Long id,
        Long projectId,
        Long modelId,
        String requestMode,
        String requestId,
        Long inputTokens,
        Long outputTokens,
        Long totalTokens,
        BigDecimal costAmount,
        String quotaCheckResult,
        String status,
        String errorMessage,
        Integer latencyMs,
        LocalDateTime occurredAt
) {
}
