package com.aiplatform.backend.dto;

import com.aiplatform.backend.entity.PlatformCredential;

import java.time.LocalDateTime;

/**
 * 平台凭证响应 DTO（不包含密钥哈希值）。
 *
 * <p>字段与 {@link PlatformCredential} entity 对齐。
 * 新设计：凭证一人一证、跨项目共用，无 boundProjectId 字段。</p>
 *
 * @param id                   凭证 ID
 * @param userId               所属用户 ID
 * @param credentialType       凭证类型（PERSONAL / SERVICE_ACCOUNT / TEMP）
 * @param keyPrefix            密钥前缀（脱敏展示）
 * @param name                 凭证名称
 * @param monthlyTokenQuota    个人月度 Token 上限（0=不限制）
 * @param usedTokensThisMonth  个人当月已消耗 Token 数
 * @param alertThresholdPct    告警阈值百分比（默认 80）
 * @param overQuotaStrategy    超配额策略
 * @param status               状态
 * @param expiresAt            过期时间
 * @param lastUsedAt           最后使用时间
 * @param createdAt            创建时间
 */
public record PlatformCredentialResponse(
        Long id,
        Long userId,
        String credentialType,
        String keyPrefix,
        String name,
        Long monthlyTokenQuota,
        Long usedTokensThisMonth,
        Integer alertThresholdPct,
        String overQuotaStrategy,
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
                credential.getMonthlyTokenQuota(),
                credential.getUsedTokensThisMonth(),
                credential.getAlertThresholdPct(),
                credential.getOverQuotaStrategy(),
                credential.getStatus(),
                credential.getExpiresAt(),
                credential.getLastUsedAt(),
                credential.getCreatedAt()
        );
    }
}
