package com.aiplatform.backend.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 刷新 Token 请求参数。
 *
 * @param refreshToken 有效的 Refresh Token（必填）
 */
public record RefreshTokenRequest(
        @NotBlank(message = "Refresh token must not be blank")
        String refreshToken
) {
}
