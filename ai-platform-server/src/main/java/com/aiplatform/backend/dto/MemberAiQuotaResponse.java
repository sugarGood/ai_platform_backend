package com.aiplatform.backend.dto;

import com.aiplatform.backend.entity.MemberAiQuota;

import java.time.LocalDateTime;

/**
 * 成员 AI 配额响应 DTO。
 *
 * <p>用于向客户端返回成员配额的详细信息，包括使用量和重置周期。</p>
 *
 * @param id          配额ID
 * @param userId      用户ID
 * @param projectId   项目ID
 * @param quotaType   配额类型
 * @param quotaLimit  配额上限
 * @param usedAmount  已使用量
 * @param resetCycle  重置周期
 * @param lastResetAt 上次重置时间
 * @param status      状态
 * @param createdAt   创建时间
 */
public record MemberAiQuotaResponse(
        Long id,
        Long userId,
        Long projectId,
        String quotaType,
        Long quotaLimit,
        Long usedAmount,
        String resetCycle,
        LocalDateTime lastResetAt,
        String status,
        LocalDateTime createdAt
) {
    /**
     * 将成员配额实体转换为响应 DTO。
     *
     * @param q 成员配额实体
     * @return 成员配额响应 DTO
     */
    public static MemberAiQuotaResponse from(MemberAiQuota q) {
        return new MemberAiQuotaResponse(
                q.getId(), q.getUserId(), q.getProjectId(), q.getQuotaType(),
                q.getQuotaLimit(), q.getUsedAmount(), q.getResetCycle(),
                q.getLastResetAt(), q.getStatus(), q.getCreatedAt()
        );
    }
}
