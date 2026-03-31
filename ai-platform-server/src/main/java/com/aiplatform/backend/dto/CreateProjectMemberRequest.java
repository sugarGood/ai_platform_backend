package com.aiplatform.backend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * 添加项目成员的请求参数。
 *
 * @param userId 用户 ID（必填）
 * @param role   项目角色简写：ADMIN / DEVELOPER / QA / PM / GUEST；未传时服务端默认 {@code DEVELOPER}
 */
public record CreateProjectMemberRequest(
        @NotNull
        Long userId,
        @Pattern(
                regexp = "ADMIN|DEVELOPER|QA|PM|GUEST",
                message = "Invalid role")
        String role
) {
}
