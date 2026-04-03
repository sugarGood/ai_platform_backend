package com.aiplatform.backend.dto;

import java.math.BigDecimal;

/**
 * 我的用量总览响应。
 */
public record MyUsageSummaryResponse(
        Long userId,
        Long totalTokens,
        Integer totalRequests,
        BigDecimal totalCost,
        String period
) {
}
