package com.aiplatform.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 修改密码请求参数。
 *
 * @param oldPassword 原密码（必填）
 * @param newPassword 新密码（必填，至少 8 位）
 */
public record ChangePasswordRequest(
        @NotBlank(message = "Old password must not be blank")
        String oldPassword,

        @NotBlank(message = "New password must not be blank")
        @Size(min = 8, message = "New password must be at least 8 characters")
        String newPassword
) {
}
