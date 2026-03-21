package com.aiplatform.backend.dto;

import com.aiplatform.backend.entity.ServiceEntity;

import java.time.LocalDateTime;

/**
 * 服务响应 DTO，用于向客户端返回项目服务详情。
 *
 * @param id          服务 ID
 * @param projectId   所属项目 ID
 * @param name        服务名称
 * @param description 服务描述
 * @param gitRepoUrl  Git 仓库地址
 * @param mainBranch  主分支名称
 * @param framework   技术框架
 * @param language    编程语言
 * @param status      服务状态
 * @param createdAt   创建时间
 */
public record ServiceResponse(
        Long id,
        Long projectId,
        String name,
        String description,
        String gitRepoUrl,
        String mainBranch,
        String framework,
        String language,
        String status,
        LocalDateTime createdAt
) {
    /**
     * 将服务实体转换为响应 DTO。
     *
     * @param entity 服务实体
     * @return 服务响应 DTO
     */
    public static ServiceResponse from(ServiceEntity entity) {
        return new ServiceResponse(
                entity.getId(),
                entity.getProjectId(),
                entity.getName(),
                entity.getDescription(),
                entity.getGitRepoUrl(),
                entity.getMainBranch(),
                entity.getFramework(),
                entity.getLanguage(),
                entity.getStatus(),
                entity.getCreatedAt()
        );
    }
}
