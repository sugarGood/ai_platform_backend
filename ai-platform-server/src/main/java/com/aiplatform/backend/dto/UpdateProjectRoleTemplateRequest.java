package com.aiplatform.backend.dto;

/**
 * 编辑项目角色模板请求。
 */
public record UpdateProjectRoleTemplateRequest(
        String templateName,
        String description,
        String status
) {
}
