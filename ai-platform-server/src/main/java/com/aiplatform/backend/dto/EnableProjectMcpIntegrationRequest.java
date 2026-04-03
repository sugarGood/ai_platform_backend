package com.aiplatform.backend.dto;

import jakarta.validation.constraints.NotNull;

/**
 * 项目启用 MCP 集成请求。
 */
public record EnableProjectMcpIntegrationRequest(
        @NotNull(message = "mcpServerId must not be null")
        Long mcpServerId
) {
}
