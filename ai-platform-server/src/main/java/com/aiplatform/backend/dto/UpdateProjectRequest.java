package com.aiplatform.backend.dto;

/**
 * 更新项目的请求参数（所有字段可选，仅更新非 null 字段）。
 *
 * @param name              项目名称
 * @param description       项目描述
 * @param icon              项目图标
 * @param ownerUserId       项目负责人用户 ID
 * @param monthlyTokenQuota 项目月度 Token 池上限
 * @param alertThresholdPct 项目池告警阈值百分比（0-100）
 * @param overQuotaStrategy 超配额策略
 */
public record UpdateProjectRequest(
        String name,
        String description,
        String icon,
        Long ownerUserId,
        Long monthlyTokenQuota,
        Integer alertThresholdPct,
        String overQuotaStrategy
) {
}
