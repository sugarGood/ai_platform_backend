package com.aiplatform.backend.dto;

import com.aiplatform.backend.entity.PermissionDefinition;

import java.time.LocalDateTime;

/**
 * 权限点信息响应DTO。
 *
 * <p>用于向客户端返回权限点的完整信息。</p>
 *
 * @param id 权限点ID
 * @param module 所属模块
 * @param permissionKey 权限点编码
 * @param name 权限点名称
 * @param description 权限点描述
 * @param permissionScope 适用范围
 * @param createdAt 创建时间
 * @param updatedAt 更新时间
 */
public record PermissionResponse(
        Long id,
        String module,
        String permissionKey,
        String name,
        String description,
        String permissionScope,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    /**
     * 将权限点定义实体转换为响应DTO。
     *
     * @param permission 权限点定义实体
     * @return 权限点响应DTO
     */
    public static PermissionResponse from(PermissionDefinition permission) {
        return new PermissionResponse(
                permission.getId(),
                permission.getModule(),
                permission.getPermissionKey(),
                permission.getName(),
                permission.getDescription(),
                permission.getPermissionScope(),
                permission.getCreatedAt(),
                permission.getUpdatedAt()
        );
    }
}
