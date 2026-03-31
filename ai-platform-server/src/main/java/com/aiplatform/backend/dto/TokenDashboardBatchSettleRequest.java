package com.aiplatform.backend.dto;

/**
 * 批量清算：可选清零项目内 {@code member_ai_quotas} 已用量，及项目成员个人池已用量。
 */
public record TokenDashboardBatchSettleRequest(
        Boolean resetMemberAiQuotaUsed,
        Boolean resetPersonalCredentialUsed
) {
}
