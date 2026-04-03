package com.aiplatform.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI 用量明细实体。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("ai_usage_events")
public class AiUsageEvent {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long credentialId;

    private Long projectId;

    private Long userId;

    private Long providerId;

    private Long providerApiKeyId;

    private Long modelId;

    private Long clientAppId;

    private Long skillId;

    private String sourceType;

    private String requestMode;

    private String requestId;

    private String conversationId;

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
