package com.aiplatform.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * AI 用量明细实体，对应 ai_usage_events 表。
 *
 * <p>记录每一次 AI 调用的详细信息，包括调用来源、模型、Token 消耗、费用和延迟等。
 * 用于用量统计、费用核算和审计追踪。</p>
 */
@TableName("ai_usage_events")
public class AiUsageEvent {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 使用的凭证ID */
    private Long credentialId;

    /** 用户ID */
    private Long userId;

    /** 项目ID */
    private Long projectId;

    /** 供应商ID */
    private Long providerId;

    /** 上游 API Key ID */
    private Long providerApiKeyId;

    /** 模型ID */
    private Long modelId;

    /** 客户端应用ID */
    private Long clientAppId;

    /** 用量来源类型 */
    private String sourceType;

    /** 请求类型 */
    private String requestMode;

    /** 请求追踪ID */
    private String requestId;

    /** 会话ID */
    private String conversationId;

    /** 触发技能ID */
    private Long skillId;

    /** 输入 Token 数 */
    private Long inputTokens;

    /** 输出 Token 数 */
    private Long outputTokens;

    /** 总 Token 数 */
    private Long totalTokens;

    /** 费用金额（USD） */
    private BigDecimal costAmount;

    /** 调用状态 */
    private String status;

    /** 错误信息 */
    private String errorMessage;

    /** 代理延迟（毫秒） */
    private Integer latencyMs;

    /** 事件发生时间 */
    private LocalDateTime occurredAt;

    /** 记录创建时间 */
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getCredentialId() { return credentialId; }
    public void setCredentialId(Long credentialId) { this.credentialId = credentialId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }
    public Long getProviderId() { return providerId; }
    public void setProviderId(Long providerId) { this.providerId = providerId; }
    public Long getProviderApiKeyId() { return providerApiKeyId; }
    public void setProviderApiKeyId(Long providerApiKeyId) { this.providerApiKeyId = providerApiKeyId; }
    public Long getModelId() { return modelId; }
    public void setModelId(Long modelId) { this.modelId = modelId; }
    public Long getClientAppId() { return clientAppId; }
    public void setClientAppId(Long clientAppId) { this.clientAppId = clientAppId; }
    public String getSourceType() { return sourceType; }
    public void setSourceType(String sourceType) { this.sourceType = sourceType; }
    public String getRequestMode() { return requestMode; }
    public void setRequestMode(String requestMode) { this.requestMode = requestMode; }
    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }
    public String getConversationId() { return conversationId; }
    public void setConversationId(String conversationId) { this.conversationId = conversationId; }
    public Long getSkillId() { return skillId; }
    public void setSkillId(Long skillId) { this.skillId = skillId; }
    public Long getInputTokens() { return inputTokens; }
    public void setInputTokens(Long inputTokens) { this.inputTokens = inputTokens; }
    public Long getOutputTokens() { return outputTokens; }
    public void setOutputTokens(Long outputTokens) { this.outputTokens = outputTokens; }
    public Long getTotalTokens() { return totalTokens; }
    public void setTotalTokens(Long totalTokens) { this.totalTokens = totalTokens; }
    public BigDecimal getCostAmount() { return costAmount; }
    public void setCostAmount(BigDecimal costAmount) { this.costAmount = costAmount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public Integer getLatencyMs() { return latencyMs; }
    public void setLatencyMs(Integer latencyMs) { this.latencyMs = latencyMs; }
    public LocalDateTime getOccurredAt() { return occurredAt; }
    public void setOccurredAt(LocalDateTime occurredAt) { this.occurredAt = occurredAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
