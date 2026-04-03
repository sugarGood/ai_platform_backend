package com.aiplatform.backend.dto;

import java.util.List;

/**
 * 项目权限矩阵响应。
 */
public record ProjectPermissionMatrixResponse(
        Long projectId,
        Long roleTemplateId,
        List<ProjectMemberModuleAccessResponse> modulePermissions
) {
}
