package com.aiplatform.backend.dto;

import com.aiplatform.backend.entity.Project;

import java.time.LocalDateTime;

/**
 * 项目响应 DTO，用于向客户端返回项目详情。
 *
 * @param id          项目 ID
 * @param name        项目名称
 * @param code        项目编码
 * @param description 项目描述
 * @param icon        项目图标
 * @param projectType 项目类型
 * @param createdBy   创建人用户 ID
 * @param ownerUserId 项目负责人用户 ID
 * @param status      项目状态
 * @param createdAt   创建时间
 * @param updatedAt   最后更新时间
 */
public record ProjectResponse(
        Long id,
        String name,
        String code,
        String description,
        String icon,
        String projectType,
        Long createdBy,
        Long ownerUserId,
        String status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    /**
     * 将项目实体转换为响应 DTO。
     *
     * @param project 项目实体
     * @return 项目响应 DTO
     */
    public static ProjectResponse from(Project project) {
        return new ProjectResponse(
                project.getId(),
                project.getName(),
                project.getCode(),
                project.getDescription(),
                project.getIcon(),
                project.getProjectType(),
                project.getCreatedBy(),
                project.getOwnerUserId(),
                project.getStatus(),
                project.getCreatedAt(),
                project.getUpdatedAt()
        );
    }
}
