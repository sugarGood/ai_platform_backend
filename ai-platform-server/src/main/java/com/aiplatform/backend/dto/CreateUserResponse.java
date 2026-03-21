package com.aiplatform.backend.dto;

import com.aiplatform.backend.entity.User;

import java.time.LocalDateTime;

/**
 * 新增用户响应 DTO。
 *
 * <p>包含用户基本信息和自动分配的平台凭证。
 * {@code credentialPlainKey} 为凭证明文密钥，<b>仅在新增时返回一次</b>，后续无法再获取，
 * 请妥善保存并告知用户。</p>
 *
 * @param id                 用户ID
 * @param email              邮箱地址
 * @param username           用户名
 * @param fullName           用户姓名
 * @param departmentId       所属部门ID
 * @param jobTitle           职位
 * @param phone              手机号
 * @param platformRole       平台角色
 * @param status             状态
 * @param createdAt          创建时间
 * @param credentialPlainKey 凭证明文密钥（仅此一次，请妥善保存）
 * @param credential         凭证详情
 */
public record CreateUserResponse(
        Long id,
        String email,
        String username,
        String fullName,
        Long departmentId,
        String jobTitle,
        String phone,
        String platformRole,
        String status,
        LocalDateTime createdAt,
        String credentialPlainKey,
        PlatformCredentialResponse credential
) {
    public static CreateUserResponse of(User user, String plainKey, PlatformCredentialResponse credential) {
        return new CreateUserResponse(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getFullName(),
                user.getDepartmentId(),
                user.getJobTitle(),
                user.getPhone(),
                user.getPlatformRole(),
                user.getStatus(),
                user.getCreatedAt(),
                plainKey,
                credential
        );
    }
}
