package com.aiplatform.backend.dto;

import jakarta.validation.constraints.NotNull;

/**
 * 项目启用技能请求。
 */
public record EnableProjectSkillRequest(
        @NotNull(message = "skillId must not be null")
        Long skillId
) {
}
