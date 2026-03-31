package com.aiplatform.backend.dto;

/**
 * 成员配额分配表一行：个人池 + 项目内 TOKEN_QUOTA 补充层。
 */
public record ProjectMemberQuotaRowResponse(
        Long memberId,
        Long userId,
        String userDisplayName,
        String role,
        /** 平台凭证 ID，无凭证为 null */
        Long credentialId,
        /** 个人月度上限（平台凭证） */
        Long personalMonthlyQuota,
        /** 个人当月已用 */
        Long personalUsedThisMonth,
        /** 剩余 = 上限 - 已用；无上限为 null */
        Long personalRemainingTokens,
        /** 项目内 TOKEN_QUOTA 记录上限，无则为 null */
        Long projectTokenQuotaLimit,
        /** member_ai_quotas.used_amount（TOKEN_QUOTA） */
        Long projectTokenQuotaUsed,
        /** 是否允许 AI：凭证存在且状态 ACTIVE */
        boolean aiAccessActive,
        /** 凭证状态码：NONE / ACTIVE / DISABLED / … */
        String credentialStatusCode
) {
}
