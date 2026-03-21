package com.aiplatform.backend.dto;

import com.aiplatform.backend.entity.AiUsageEvent;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * AI 用量明细响应 DTO。
 *
 * <p>包含双池配额检查结果 {@code quotaCheckResult}，
 * 可用于前端展示告警状态或拦截原因。</p>
 *
 * @param id               事件ID
 * @param credentialId     使用的凭证ID（个人池来源）
 * @param userId           用户ID
 * @param projectId        项目ID（项目池来源）
 * @param providerId       供应商ID
 * @param modelId          模型ID
 * @param sourceType       用量来源类型
 * @param requestMode      请求类型
 * @param requestId        请求追踪ID
 * @param skillId          触发技能ID
 * @param inputTokens      输入 Token 数
 * @param outputTokens     输出 Token 数
 * @param totalTokens      总 Token 数
 * @param costAmount       费用金额（USD）
 * @param quotaCheckResult 双池配额检查结果
 * @param status           调用状态
 * @param errorMessage     错误信息
 * @param latencyMs        代理延迟（毫秒）
 * @param occurredAt       事件发生时间
 */
public record AiUsageEventResponse(
        Long id,
        Long credentialId,
        Long userId,
        Long projectId,
        Long providerId,
        Long modelId,
        String sourceType,
        String requestMode,
        String requestId,
        Long skillId,
        Long inputTokens,
        Long outputTokens,
        Long totalTokens,
        BigDecimal costAmount,
        String quotaCheckResult,
        String status,
        String errorMessage,
        Integer latencyMs,
        LocalDateTime occurredAt
) {
    public static AiUsageEventResponse from(AiUsageEvent e) {
        return new AiUsageEventResponse(
                e.getId(), e.getCredentialId(), e.getUserId(), e.getProjectId(),
                e.getProviderId(), e.getModelId(), e.getSourceType(),
                e.getRequestMode(), e.getRequestId(), e.getSkillId(),
                e.getInputTokens(), e.getOutputTokens(), e.getTotalTokens(),
                e.getCostAmount(), e.getQuotaCheckResult(), e.getStatus(),
                e.getErrorMessage(), e.getLatencyMs(), e.getOccurredAt()
        );
    }
}
