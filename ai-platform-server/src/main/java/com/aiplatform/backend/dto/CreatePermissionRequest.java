package com.aiplatform.backend.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 创建权限点的请求参数。
 *
 * @param module 权限所属模块，如 knowledge、model，不可为空
 * @param permissionKey 权限点编码，全局唯一，如 knowledge.upload，不可为空
 * @param name 权限点名称，用于界面展示，不可为空
 * @param description 权限点描述，可选
 * @param permissionScope 适用范围：PLATFORM 或 PROJECT，可选
 */
public record CreatePermissionRequest(
        @NotBlank(message = "Module must not be blank")
        String module,
        @NotBlank(message = "Permission key must not be blank")
        String permissionKey,
        @NotBlank(message = "Permission name must not be blank")
        String name,
        String description,
        String permissionScope
) {
}
