package com.aiplatform.agent.gateway.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * AI 用量事件实体（网关写入）。
 *
 * <p>映射 {@code ai_usage_events} 表，网关在每次成功调用上游供应商后，
 * 将本次请求的用量信息写入该表，用于后续的用量统计、计费和审计。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("ai_usage_events")
public class AiUsageEventRef {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long credentialId;
    private Long userId;
    private Long projectId;
    private Long providerId;
    private Long providerApiKeyId;
    private Long modelId;
    private String sourceType;
    private String requestMode;
    private String requestId;
    private Long inputTokens;
    private Long outputTokens;
    private Long totalTokens;
    private BigDecimal costAmount;
    private String quotaCheckResult;
    private String status;
    private String errorMessage;
    private Integer latencyMs;
    private LocalDateTime occurredAt;
    private LocalDateTime createdAt;
}
