package com.aiplatform.backend.dto;

/**
 * 项目角色模板响应。
 */
public record ProjectRoleTemplateResponse(
        Long id,
        Long projectId,
        String roleCode,
        String templateName,
        String description,
        String status
) {
}
