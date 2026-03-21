package com.aiplatform.backend.dto;

import com.aiplatform.backend.entity.User;

import java.time.LocalDateTime;

/**
 * 用户信息响应 DTO。
 * <p>封装用户实体的全部字段，用于 REST 接口返回。</p>
 *
 * @param id           用户ID
 * @param email        邮箱地址
 * @param username     用户名
 * @param fullName     用户姓名
 * @param avatarUrl    头像URL
 * @param departmentId 所属部门ID
 * @param jobTitle     职位
 * @param phone        手机号
 * @param platformRole 平台角色
 * @param status       状态
 * @param createdAt    创建时间
 * @param updatedAt    更新时间
 */
public record UserResponse(
        Long id,
        String email,
        String username,
        String fullName,
        String avatarUrl,
        Long departmentId,
        String jobTitle,
        String phone,
        String platformRole,
        String status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    /**
     * 将用户实体转换为响应 DTO。
     *
     * @param user 用户实体
     * @return 用户响应 DTO
     */
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getFullName(),
                user.getAvatarUrl(),
                user.getDepartmentId(),
                user.getJobTitle(),
                user.getPhone(),
                user.getPlatformRole(),
                user.getStatus(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
