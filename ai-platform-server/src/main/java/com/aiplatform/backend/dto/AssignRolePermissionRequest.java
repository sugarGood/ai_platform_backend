package com.aiplatform.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 分配角色权限的请求参数。
 *
 * <p>用于为指定角色分配特定权限点及其访问级别。</p>
 *
 * @param roleId 角色ID，不可为空
 * @param permissionId 权限点ID，不可为空
 * @param accessLevel 访问级别：NONE / VIEW / CALL / CREATE / FULL_CONTROL，不可为空
 */
public record AssignRolePermissionRequest(
        @NotNull(message = "Role ID must not be null")
        Long roleId,
        @NotNull(message = "Permission ID must not be null")
        Long permissionId,
        @NotBlank(message = "Access level must not be blank")
        String accessLevel
) {
}
