package com.aiplatform.backend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * 添加项目成员的请求参数。
 *
 * @param userId 用户 ID（必填）
 * @param role   成员角色：ADMIN / MEMBER / VIEWER（默认 MEMBER）
 */
public record CreateProjectMemberRequest(
        @NotNull
        Long userId,
        @Pattern(regexp = "ADMIN|MEMBER|VIEWER", message = "Invalid role")
        String role
) {
}
