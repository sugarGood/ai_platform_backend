package com.aiplatform.backend.dto;

import com.aiplatform.backend.entity.Role;

import java.time.LocalDateTime;

/**
 * 角色信息响应DTO。
 *
 * <p>用于向客户端返回角色的完整信息，包括角色编码、适用范围、配额等。</p>
 *
 * @param id 角色ID
 * @param name 角色名称
 * @param code 角色编码
 * @param roleScope 适用范围：PLATFORM 或 PROJECT
 * @param description 角色描述
 * @param isSystem 是否为系统内置角色
 * @param defaultQuotaTokens 默认月Token配额
 * @param status 角色状态
 * @param createdAt 创建时间
 * @param updatedAt 更新时间
 */
public record RoleResponse(
        Long id,
        String name,
        String code,
        String roleScope,
        String description,
        Boolean isSystem,
        Long defaultQuotaTokens,
        String status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    /**
     * 将角色实体转换为响应DTO。
     *
     * @param role 角色实体
     * @return 角色响应DTO
     */
    public static RoleResponse from(Role role) {
        return new RoleResponse(
                role.getId(),
                role.getName(),
                role.getCode(),
                role.getRoleScope(),
                role.getDescription(),
                role.getIsSystem(),
                role.getDefaultQuotaTokens(),
                role.getStatus(),
                role.getCreatedAt(),
                role.getUpdatedAt()
        );
    }
}
