package com.aiplatform.backend.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 更新用户平台角色（与 {@code roles.code} 中 {@code role_scope = PLATFORM} 的编码一致）。
 *
 * @param platformRole 目标平台角色编码，如 MEMBER、PLATFORM_ADMIN、SUPER_ADMIN
 */
public record UpdateUserPlatformRoleRequest(
        @NotBlank(message = "Platform role must not be blank")
        String platformRole
) {
}
