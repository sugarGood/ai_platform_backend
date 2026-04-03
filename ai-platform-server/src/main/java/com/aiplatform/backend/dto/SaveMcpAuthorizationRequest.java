package com.aiplatform.backend.dto;

/**
 * MCP 授权保存请求。
 */
public record SaveMcpAuthorizationRequest(
        Long projectId,
        String authConfig,
        String authStatus,
        String accessScope,
        String expiresAt
) {
}
