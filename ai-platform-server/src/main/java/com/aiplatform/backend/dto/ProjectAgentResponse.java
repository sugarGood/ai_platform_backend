package com.aiplatform.backend.dto;

import com.aiplatform.backend.entity.ProjectAgent;

import java.time.LocalDateTime;

/**
 * 项目专属智能体响应 DTO。
 */
public record ProjectAgentResponse(
        Long id,
        Long projectId,
        String name,
        String description,
        String avatarIcon,
        String systemPrompt,
        String preferredModel,
        Boolean enableRag,
        Boolean enableSkills,
        Boolean enableTools,
        Boolean enableDeploy,
        Boolean enableMonitoring,
        String status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ProjectAgentResponse from(ProjectAgent agent) {
        return new ProjectAgentResponse(
                agent.getId(),
                agent.getProjectId(),
                agent.getName(),
                agent.getDescription(),
                agent.getAvatarIcon(),
                agent.getSystemPrompt(),
                agent.getPreferredModel(),
                agent.getEnableRag(),
                agent.getEnableSkills(),
                agent.getEnableTools(),
                agent.getEnableDeploy(),
                agent.getEnableMonitoring(),
                agent.getStatus(),
                agent.getCreatedAt(),
                agent.getUpdatedAt()
        );
    }
}
