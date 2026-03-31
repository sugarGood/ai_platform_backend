package com.aiplatform.backend.dto;

import jakarta.validation.constraints.NotNull;

/** 切换成员 AI 调用开关（映射平台凭证 ACTIVE / DISABLED）。 */
public record ProjectMemberAiAccessRequest(@NotNull Boolean enabled) {
}
