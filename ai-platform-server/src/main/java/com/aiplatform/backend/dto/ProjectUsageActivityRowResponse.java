package com.aiplatform.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 活跃日志行：在 {@link AiUsageEventResponse} 基础上补充展示字段。
 */
public record ProjectUsageActivityRowResponse(
        Long id,
        Long userId,
        String userDisplayName,
        Long projectId,
        Long totalTokens,
        String sourceType,
        String requestMode,
        String status,
        String quotaCheckResult,
        String activitySummary,
        String activityTypeLabel,
        BigDecimal costAmount,
        LocalDateTime occurredAt
) {
}
