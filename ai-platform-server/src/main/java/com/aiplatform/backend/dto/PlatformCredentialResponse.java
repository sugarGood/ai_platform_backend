package com.aiplatform.backend.dto;

import com.aiplatform.backend.entity.PlatformCredential;

import java.time.LocalDateTime;

/**
 * 平台凭证响应 DTO（不包含密钥哈希值）。
 *
 * @param id             凭证 ID
 * @param userId         所属用户 ID
 * @param credentialType 凭证类型
 * @param keyPrefix      密钥前缀（脱敏展示）
 * @param name           凭证名称
 * @param boundProjectId 绑定项目 ID
 * @param status         状态
 * @param expiresAt      过期时间
 * @param lastUsedAt     最后使用时间
 * @param createdAt      创建时间
 */
public record PlatformCredentialResponse(
        Long id,
        Long userId,
        String credentialType,
        String keyPrefix,
        String name,
        Long boundProjectId,
        String status,
        LocalDateTime expiresAt,
        LocalDateTime lastUsedAt,
        LocalDateTime createdAt
) {
    public static PlatformCredentialResponse from(PlatformCredential credential) {
        return new PlatformCredentialResponse(
                credential.getId(),
                credential.getUserId(),
                credential.getCredentialType(),
                credential.getKeyPrefix(),
                credential.getName(),
                credential.getBoundProjectId(),
                credential.getStatus(),
                credential.getExpiresAt(),
                credential.getLastUsedAt(),
                credential.getCreatedAt()
        );
    }
}
