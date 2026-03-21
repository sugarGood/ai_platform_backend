package com.aiplatform.agent.gateway.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * AI 用量事件实体（网关写入）。
 *
 * <p>映射 {@code ai_usage_events} 表，网关在每次成功调用上游供应商后，
 * 将本次请求的用量信息写入该表，用于后续的用量统计、计费和审计。</p>
 */
@TableName("ai_usage_events")
public class AiUsageEventRef {

    /** 主键 ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 调用方凭证 ID */
    private Long credentialId;

    /** 调用方用户 ID */
    private Long userId;

    /** 关联项目 ID */
    private Long projectId;

    /** 上游供应商 ID */
    private Long providerId;

    /** 使用的上游 API 密钥 ID */
    private Long providerApiKeyId;

    /** 调用的模型 ID */
    private Long modelId;

    /** 来源类型（如 PLATFORM_GATEWAY） */
    private String sourceType;

    /** 请求模式（如 CHAT、EMBEDDING） */
    private String requestMode;

    /** 请求唯一标识，用于链路追踪 */
    private String requestId;

    /** 输入 Token 数 */
    private Long inputTokens;

    /** 输出 Token 数 */
    private Long outputTokens;

    /** 总 Token 数 */
    private Long totalTokens;

    /** 本次调用费用 */
    private BigDecimal costAmount;

    /** 调用状态（如 SUCCESS、FAILED） */
    private String status;

    /** 错误信息，调用失败时记录 */
    private String errorMessage;

    /** 请求延迟（毫秒） */
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
    public String getSourceType() { return sourceType; }
    public void setSourceType(String sourceType) { this.sourceType = sourceType; }
    public String getRequestMode() { return requestMode; }
    public void setRequestMode(String requestMode) { this.requestMode = requestMode; }
    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }
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
