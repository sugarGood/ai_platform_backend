package com.aiplatform.backend.dto;

import com.aiplatform.backend.entity.Project;

import java.util.List;

/**
 * 项目工作台卡片聚合数据，用于一页展示项目网格。
 *
 * @param id                 项目 ID
 * @param name               项目名称
 * @param code               项目编码
 * @param description        项目描述
 * @param icon               展示用图标（emoji 或 icon key）
 * @param projectType        原始类型：PRODUCT / PLATFORM / DATA / OTHER
 * @param projectTypeLabel   中文展示标签（如 产品项目）
 * @param status             项目状态
 * @param codeServiceCount   代码服务（ACTIVE）数量
 * @param ai                 AI 能力统计
 * @param token              本月 Token 用量与配额
 * @param memberCount        成员总数
 * @param memberAvatars      成员头像预览 URL（至多若干条，用于叠放展示）
 */
public record ProjectCardResponse(
        Long id,
        String name,
        String code,
        String description,
        String icon,
        String projectType,
        String projectTypeLabel,
        String status,
        long codeServiceCount,
        ProjectCardAiMetrics ai,
        ProjectCardTokenUsage token,
        long memberCount,
        List<String> memberAvatars
) {
    public static ProjectCardResponse of(
            Project project,
            String projectTypeLabel,
            long codeServiceCount,
            ProjectCardAiMetrics ai,
            ProjectCardTokenUsage token,
            long memberCount,
            List<String> memberAvatars) {
        return new ProjectCardResponse(
                project.getId(),
                project.getName(),
                project.getCode(),
                project.getDescription(),
                project.getIcon(),
                project.getProjectType(),
                projectTypeLabel,
                project.getStatus(),
                codeServiceCount,
                ai,
                token,
                memberCount,
                memberAvatars
        );
    }
}
