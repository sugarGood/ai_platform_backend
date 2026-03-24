package com.aiplatform.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * 登录请求参数。
 *
 * @param email    登录邮箱（必填）
 * @param password 登录密码（必填）
 */
public record LoginRequest(
        @NotBlank(message = "Email must not be blank")
        @Email(message = "Email format is invalid")
        String email,

        @NotBlank(message = "Password must not be blank")
        String password
) {
}
