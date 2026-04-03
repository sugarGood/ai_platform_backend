package com.aiplatform.backend.dto;

import jakarta.validation.constraints.NotNull;

/**
 * 项目启用工具请求。
 */
public record EnableProjectToolRequest(
        @NotNull(message = "toolId must not be null")
        Long toolId
) {
}
