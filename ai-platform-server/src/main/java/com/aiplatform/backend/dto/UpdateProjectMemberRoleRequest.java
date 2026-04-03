package com.aiplatform.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * 更新项目成员角色的请求参数。
 *
 * @param role 项目角色简码：ADMIN / DEVELOPER / QA / PM / GUEST
 */
public record UpdateProjectMemberRoleRequest(
        @NotBlank(message = "Role must not be blank")
        @Pattern(
                regexp = "ADMIN|DEVELOPER|QA|PM|GUEST",
                message = "Invalid role")
        String role,
        Boolean resetAbilitiesToRoleDefault
) {
}
