package com.aiplatform.backend.dto;

public record ProjectMemberTokenQuotaSummaryResponse(
        Long credentialId,
        String credentialStatus,
        Long personalMonthlyQuota,
        Long personalUsedThisMonth,
        Long personalRemainingTokens,
        Integer personalAlertThresholdPct,
        String personalOverQuotaStrategy,
        Long projectTokenQuotaLimit,
        Long projectTokenQuotaUsed,
        Long projectTokenQuotaRemaining,
        String projectTokenQuotaStatus
) {
}
