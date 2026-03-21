package com.aiplatform.backend.dto;

import jakarta.validation.constraints.NotNull;

/**
 * 创建用户客户端绑定的请求参数。
 *
 * @param userId      用户 ID（必填）
 * @param clientAppId 客户端 ID（必填）
 */
public record CreateUserClientBindingRequest(
        @NotNull(message = "User ID must not be null")
        Long userId,
        @NotNull(message = "Client app ID must not be null")
        Long clientAppId
) {
}
