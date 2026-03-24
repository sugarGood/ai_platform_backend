package com.aiplatform.backend.dto;

/**
 * 项目概览聚合响应 DTO。
 *
 * @param projectId            项目 ID
 * @param name                 项目名称
 * @param status               项目状态
 * @param memberCount          成员数量
 * @param serviceCount         代码服务数量
 * @param monthlyTokenQuota    月度 Token 配额上限
 * @param usedTokensThisMonth  本月已用 Token 数
 * @param usedPct              已用百分比（0-100），配额为0时返回 null
 * @param overQuotaStrategy    超配额策略
 */
public record ProjectOverviewResponse(
        Long projectId,
        String name,
        String status,
        long memberCount,
        long serviceCount,
        Long monthlyTokenQuota,
        Long usedTokensThisMonth,
        Double usedPct,
        String overQuotaStrategy
) {
}
