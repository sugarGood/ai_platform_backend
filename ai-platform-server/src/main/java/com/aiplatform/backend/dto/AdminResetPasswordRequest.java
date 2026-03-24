package com.aiplatform.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 管理员为指定用户设置新登录密码（无需原密码）。
 *
 * @param newPassword 新密码（明文，至少 8 位，将以 BCrypt 存储）
 */
public record AdminResetPasswordRequest(
        @NotBlank(message = "New password must not be blank")
        @Size(min = 8, message = "New password must be at least 8 characters")
        String newPassword
) {
}
