package com.aiplatform.backend.dto;

import java.math.BigDecimal;

/**
 * 按项目用量分布行。
 */
public record UsageProjectDistributionRowResponse(
        Long projectId,
        Long totalTokens,
        Integer totalRequests,
        BigDecimal totalCost
) {
}
