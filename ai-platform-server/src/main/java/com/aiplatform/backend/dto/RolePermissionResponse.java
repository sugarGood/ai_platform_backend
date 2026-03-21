package com.aiplatform.backend.dto;

import com.aiplatform.backend.entity.RolePermission;

import java.time.LocalDateTime;

/**
 * 角色-权限关联响应DTO。
 *
 * <p>用于向客户端返回角色与权限点的关联信息及其访问级别。</p>
 *
 * @param id 关联记录ID
 * @param roleId 角色ID
 * @param permissionId 权限点ID
 * @param accessLevel 访问级别
 * @param createdAt 创建时间
 * @param updatedAt 更新时间
 */
public record RolePermissionResponse(
        Long id,
        Long roleId,
        Long permissionId,
        String accessLevel,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    /**
     * 将角色-权限关联实体转换为响应DTO。
     *
     * @param rp 角色-权限关联实体
     * @return 角色-权限关联响应DTO
     */
    public static RolePermissionResponse from(RolePermission rp) {
        return new RolePermissionResponse(
                rp.getId(),
                rp.getRoleId(),
                rp.getPermissionId(),
                rp.getAccessLevel(),
                rp.getCreatedAt(),
                rp.getUpdatedAt()
        );
    }
}
