package com.aiplatform.backend.dto;

/**
 * 登录成功响应。
 *
 * @param accessToken  Access Token（Bearer，有效期由配置决定，默认 24h）
 * @param refreshToken Refresh Token（用于续期，默认 7d）
 * @param tokenType    固定值 {@code Bearer}
 * @param expiresIn    Access Token 有效秒数
 * @param user         当前登录用户信息
 */
public record LoginResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn,
        UserResponse user
) {
    /**
     * 快捷工厂方法。
     *
     * @param accessToken  Access Token
     * @param refreshToken Refresh Token
     * @param expiresIn    Access Token 有效秒数
     * @param user         登录用户响应 DTO
     * @return 登录响应
     */
    public static LoginResponse of(String accessToken, String refreshToken, long expiresIn, UserResponse user) {
        return new LoginResponse(accessToken, refreshToken, "Bearer", expiresIn, user);
    }
}
