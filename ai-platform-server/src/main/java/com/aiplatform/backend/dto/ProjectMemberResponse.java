package com.aiplatform.backend.dto;

import com.aiplatform.backend.entity.PlatformCredential;
import com.aiplatform.backend.entity.ProjectMember;

import java.time.LocalDateTime;

/**
 * 项目成员响应 DTO，用于向客户端返回成员信息。
 *
 * @param id                       成员记录 ID
 * @param projectId                所属项目 ID
 * @param userId                   用户 ID
 * @param role                     项目角色简写：ADMIN / DEVELOPER / QA / PM / GUEST
 * @param joinedAt                 加入项目时间
 * @param credentialStatus         平台凭证状态码：NONE / VALID / EXPIRING_SOON / EXPIRED / REVOKED / DISABLED
 * @param credentialExpiresInDays  距凭证过期剩余自然日，无过期或未过期场景为 {@code null}
 * @param credentialExpiresAt      凭证过期时间，无则为 {@code null}
 */
public record ProjectMemberResponse(
        Long id,
        Long projectId,
        Long userId,
        String role,
        LocalDateTime joinedAt,
        String credentialStatus,
        Integer credentialExpiresInDays,
        LocalDateTime credentialExpiresAt
) {
    /**
     * 将项目成员实体转换为响应 DTO（不含凭证信息，等价于无凭证 {@code NONE}）。
     */
    public static ProjectMemberResponse from(ProjectMember member) {
        return from(member, null);
    }

    /**
     * 将项目成员与用户平台凭证合并为响应 DTO（一人一证，跨项目共用）。
     */
    public static ProjectMemberResponse from(ProjectMember member, PlatformCredential credential) {
        return new ProjectMemberResponse(
                member.getId(),
                member.getProjectId(),
                member.getUserId(),
                member.getRole(),
                member.getJoinedAt(),
                ProjectMemberCredentialView.status(credential),
                ProjectMemberCredentialView.expiresInDays(credential),
                ProjectMemberCredentialView.expiresAt(credential)
        );
    }
}
