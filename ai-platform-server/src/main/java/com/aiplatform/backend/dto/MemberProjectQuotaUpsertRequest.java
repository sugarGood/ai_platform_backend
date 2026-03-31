package com.aiplatform.backend.dto;

import jakarta.validation.constraints.NotNull;

/**
 * 在项目内为成员设置 AI 配额的请求体（不含 userId/projectId，由路径推导）。
 */
public record MemberProjectQuotaUpsertRequest(
        @NotNull String quotaType,
        @NotNull Long quotaLimit,
        String resetCycle
) {
}
