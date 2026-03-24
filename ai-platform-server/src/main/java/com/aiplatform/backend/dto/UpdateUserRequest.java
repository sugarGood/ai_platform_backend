package com.aiplatform.backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 更新用户信息的请求参数。
 * <p>仅更新非空字段，为 {@code null} 的字段将被忽略（字符串会先 {@code trim}，空串视为不修改）。</p>
 *
 * @param fullName            用户姓名
 * @param avatarUrl           头像 URL
 * @param departmentId        所属部门 ID
 * @param jobTitle            职位
 * @param phone               手机号
 * @param email               登录邮箱（须全局唯一）
 * @param username            登录用户名（须全局唯一）
 * @param credentialName      个人平台凭证展示名称
 * @param monthlyTokenQuota   个人凭证月度 Token 上限；{@code 0} 表示不限制
 * @param alertThresholdPct   告警阈值 0–100
 * @param overQuotaStrategy   超配额策略
 */
public record UpdateUserRequest(
        String fullName,
        String avatarUrl,
        Long departmentId,
        String jobTitle,
        String phone,
        @Pattern(
                regexp = "^$|^[\\w.!#$%&'*+/=?^`{|}~-]+@[\\w-]+(?:\\.[\\w-]+)+$",
                message = "Invalid email format")
        String email,
        @Size(min = 1, max = 64, message = "Username length must be 1–64")
        String username,
        String credentialName,
        @Min(value = 0, message = "Monthly token quota must be >= 0")
        Long monthlyTokenQuota,
        @Min(0)
        @Max(100)
        Integer alertThresholdPct,
        String overQuotaStrategy
) {
}
