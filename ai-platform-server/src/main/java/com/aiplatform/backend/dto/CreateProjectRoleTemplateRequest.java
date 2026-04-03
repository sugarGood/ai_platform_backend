package com.aiplatform.backend.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 新增项目角色模板请求。
 */
public record CreateProjectRoleTemplateRequest(
        @NotBlank(message = "roleCode must not be blank")
        String roleCode,
        @NotBlank(message = "templateName must not be blank")
        String templateName,
        String description
) {
}
