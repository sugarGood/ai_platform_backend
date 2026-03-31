package com.aiplatform.backend.dto;

/**
 * 按成员汇总的当月消耗，用于部门/角色条形图。
 */
public record ProjectConsumptionByUserRowResponse(
        Long userId,
        String userDisplayName,
        Long departmentId,
        String role,
        long tokensThisMonth,
        /** 从本月 1 日到当前日期的日均估算 */
        double estimatedTokensPerDay
) {
}
