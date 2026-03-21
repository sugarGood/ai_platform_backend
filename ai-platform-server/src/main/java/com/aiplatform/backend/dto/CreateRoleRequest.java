package com.aiplatform.backend.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 创建角色的请求参数。
 *
 * @param name 角色名称，不可为空
 * @param code 角色编码（唯一标识），如 SUPER_ADMIN，不可为空
 * @param roleScope 适用范围：PLATFORM（平台级）或 PROJECT（项目级），不可为空
 * @param description 角色描述，可选
 * @param defaultQuotaTokens 该角色的默认月Token配额，可选
 */
public record CreateRoleRequest(
        @NotBlank(message = "Role name must not be blank")
        String name,
        @NotBlank(message = "Role code must not be blank")
        String code,
        @NotBlank(message = "Role scope must not be blank")
        String roleScope,
        String description,
        Long defaultQuotaTokens
) {
}
