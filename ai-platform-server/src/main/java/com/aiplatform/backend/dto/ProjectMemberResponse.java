package com.aiplatform.backend.dto;

import com.aiplatform.backend.entity.ProjectMember;

import java.time.LocalDateTime;

/**
 * 项目成员响应 DTO，用于向客户端返回成员信息。
 *
 * @param id        成员记录 ID
 * @param projectId 所属项目 ID
 * @param userId    用户 ID
 * @param role      成员角色
 * @param joinedAt  加入项目时间
 */
public record ProjectMemberResponse(
        Long id,
        Long projectId,
        Long userId,
        String role,
        LocalDateTime joinedAt
) {
    /**
     * 将项目成员实体转换为响应 DTO。
     *
     * @param member 项目成员实体
     * @return 项目成员响应 DTO
     */
    public static ProjectMemberResponse from(ProjectMember member) {
        return new ProjectMemberResponse(
                member.getId(),
                member.getProjectId(),
                member.getUserId(),
                member.getRole(),
                member.getJoinedAt()
        );
    }
}
