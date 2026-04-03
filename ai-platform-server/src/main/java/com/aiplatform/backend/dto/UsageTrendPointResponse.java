package com.aiplatform.backend.dto;

/**
 * 我的用量趋势数据点。
 */
public record UsageTrendPointResponse(
        String date,
        Long totalTokens,
        Integer totalRequests
) {
}
