package com.aiplatform.backend.dto.me;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 当前登录用户凭证聚合响应。
 *
 * @param credentialId      凭证 ID
 * @param userId            用户 ID
 * @param keyPrefix         凭证前缀（脱敏）
 * @param credentialType    凭证类型
 * @param status            凭证状态
 * @param expiresAt         过期时间
 * @param defaultProjectId  当前工作项目 ID
 * @param defaultProject    当前工作项目对象
 * @param accessibleProjects 可访问项目列表
 */
public record MeCredentialResponse(
        Long credentialId,
        Long userId,
        String keyPrefix,
        String credentialType,
        String status,
        LocalDateTime expiresAt,
        Long defaultProjectId,
        MeProjectOptionResponse defaultProject,
        List<MeProjectOptionResponse> accessibleProjects
) {
}
