package com.aiplatform.backend.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 邀请用户加入平台的请求参数。
 *
 * @param email        邮箱地址（必填），用作登录标识
 * @param username     用户名（必填）
 * @param fullName     用户姓名
 * @param departmentId 所属部门ID
 * @param jobTitle     职位
 * @param phone        手机号
 * @param platformRole 平台角色，默认为 MEMBER
 */
public record InviteUserRequest(
        @NotBlank(message = "Email must not be blank")
        String email,
        @NotBlank(message = "Username must not be blank")
        String username,
        String fullName,
        Long departmentId,
        String jobTitle,
        String phone,
        String platformRole
) {
}
