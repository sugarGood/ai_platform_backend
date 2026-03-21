package com.aiplatform.backend.dto;

import com.aiplatform.backend.entity.AiUsageEvent;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * AI 用量明细响应 DTO。
 *
 * <p>用于向客户端返回 AI 调用的详细记录，包括 Token 消耗、费用和延迟等指标。</p>
 *
 * @param id           事件ID
 * @param credentialId 使用的凭证ID
 * @param userId       用户ID
 * @param projectId    项目ID
 * @param providerId   供应商ID
 * @param modelId      模型ID
 * @param sourceType   用量来源类型
 * @param requestMode  请求类型
 * @param requestId    请求追踪ID
 * @param skillId      触发技能ID
 * @param inputTokens  输入 Token 数
 * @param outputTokens 输出 Token 数
 * @param totalTokens  总 Token 数
 * @param costAmount   费用金额（USD）
 * @param status       调用状态
 * @param errorMessage 错误信息
 * @param latencyMs    代理延迟（毫秒）
 * @param occurredAt   事件发生时间
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
        String status,
        String errorMessage,
        Integer latencyMs,
        LocalDateTime occurredAt
) {
    /**
     * 将 AI 用量事件实体转换为响应 DTO。
     *
     * @param e AI 用量事件实体
     * @return AI 用量事件响应 DTO
     */
    public static AiUsageEventResponse from(AiUsageEvent e) {
        return new AiUsageEventResponse(
                e.getId(), e.getCredentialId(), e.getUserId(), e.getProjectId(),
                e.getProviderId(), e.getModelId(), e.getSourceType(),
                e.getRequestMode(), e.getRequestId(), e.getSkillId(),
                e.getInputTokens(), e.getOutputTokens(), e.getTotalTokens(),
                e.getCostAmount(), e.getStatus(), e.getErrorMessage(),
                e.getLatencyMs(), e.getOccurredAt()
        );
    }
}
