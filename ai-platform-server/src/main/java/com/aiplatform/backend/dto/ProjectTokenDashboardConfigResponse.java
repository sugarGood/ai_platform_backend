package com.aiplatform.backend.dto;

/**
 * 仪表盘「项目/配置」区块：与 {@link com.aiplatform.backend.entity.Project} 对齐并附带展示用派生字段。
 */
public record ProjectTokenDashboardConfigResponse(
        Long projectId,
        String projectName,
        Long monthlyTokenQuota,
        Long usedTokensThisMonth,
        Long remainingTokens,
        Integer alertThresholdPct,
        String overQuotaStrategy,
        /** DAILY / WEEKLY / MONTHLY */
        String quotaResetCycle,
        /** 单次请求上限；effective 为 null 时的展示用默认值 */
        Long singleRequestTokenCap,
        Long effectiveSingleRequestTokenCap,
        String status
) {
}
