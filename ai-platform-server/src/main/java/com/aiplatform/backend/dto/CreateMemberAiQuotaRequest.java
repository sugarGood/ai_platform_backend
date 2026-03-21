package com.aiplatform.backend.dto;

import jakarta.validation.constraints.NotNull;

/**
 * 创建成员 AI 配额请求 DTO。
 *
 * @param userId     用户ID（必填）
 * @param projectId  项目ID，为 NULL 时表示个人总配额
 * @param quotaType  配额类型：TOKEN_QUOTA / COST_QUOTA / REQUEST_QUOTA（必填）
 * @param quotaLimit 配额上限（必填）
 * @param resetCycle 重置周期：DAILY / WEEKLY / MONTHLY，默认 MONTHLY
 */
public record CreateMemberAiQuotaRequest(
        @NotNull Long userId,
        Long projectId,
        @NotNull String quotaType,
        @NotNull Long quotaLimit,
        String resetCycle
) {
}
