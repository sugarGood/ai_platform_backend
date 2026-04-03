package com.aiplatform.backend.dto;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 保存项目角色模板权限矩阵请求。
 */
public record ProjectRoleTemplatePermissionsRequest(
        @Valid List<ProjectMemberPermissionOverrideItemRequest> modulePermissions
) {
}
