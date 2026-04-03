package com.aiplatform.backend.dto;

public record ProjectMemberQuotaOverrideRequest(
        Long personalMonthlyQuota,
        Integer personalAlertThresholdPct,
        String personalOverQuotaStrategy,
        Long projectTokenQuotaLimit,
        String projectTokenQuotaResetCycle,
        String projectTokenQuotaStatus
) {
}
