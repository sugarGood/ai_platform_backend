package com.aiplatform.backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 新增用户请求参数。
 *
 * @param email               邮箱地址（必填），用作登录标识
 * @param username            用户名（必填）
 * @param password            登录密码（必填，至少 8 位），将以 BCrypt 哈希存储
 * @param fullName            用户姓名
 * @param avatarUrl           头像 URL
 * @param departmentId        所属部门ID
 * @param jobTitle            职位
 * @param phone               手机号
 * @param platformRole        平台角色，默认为 MEMBER
 * @param credentialName      个人平台凭证展示名称，默认「姓名/用户名 + 的凭证」
 * @param monthlyTokenQuota   个人凭证月度 Token 上限；{@code 0} 表示不限制；{@code null} 使用默认 200K
 * @param alertThresholdPct   个人凭证配额告警阈值（0–100），{@code null} 默认 80
 * @param overQuotaStrategy   超配额策略（如 BLOCK），{@code null} 使用默认
 */
public record CreateUserRequest(
        @NotBlank(message = "Email must not be blank")
        String email,
        @NotBlank(message = "Username must not be blank")
        String username,
        @NotBlank(message = "Password must not be blank")
        @Size(min = 8, message = "Password must be at least 8 characters")
        String password,
        String fullName,
        String avatarUrl,
        Long departmentId,
        String jobTitle,
        String phone,
        String platformRole,
        String credentialName,
        @Min(value = 0, message = "Monthly token quota must be >= 0")
        Long monthlyTokenQuota,
        @Min(0)
        @Max(100)
        Integer alertThresholdPct,
        String overQuotaStrategy
) {
}
