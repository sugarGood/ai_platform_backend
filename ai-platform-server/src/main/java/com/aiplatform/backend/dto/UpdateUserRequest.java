package com.aiplatform.backend.dto;

/**
 * 更新用户信息的请求参数。
 * <p>仅更新非空字段，为 {@code null} 的字段将被忽略。</p>
 *
 * @param fullName     用户姓名
 * @param avatarUrl    头像URL
 * @param departmentId 所属部门ID
 * @param jobTitle     职位
 * @param phone        手机号
 */
public record UpdateUserRequest(
        String fullName,
        String avatarUrl,
        Long departmentId,
        String jobTitle,
        String phone
) {
}
