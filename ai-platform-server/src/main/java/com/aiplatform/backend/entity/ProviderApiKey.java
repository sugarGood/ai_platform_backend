package com.aiplatform.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * 上游 API 密钥实体，对应 provider_api_keys 表。
 * <p>管理平台对接各 AI 供应商时使用的 API 密钥，包含配额、限流和代理配置。</p>
 */
@TableName("provider_api_keys")
public class ProviderApiKey {

    /** 主键ID，自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属供应商ID，关联 ai_providers 表 */
    private Long providerId;

    /** 密钥标识名称，便于管理区分 */
    private String label;

    /** 密钥前缀，用于安全展示（如 sk-abc123...） */
    private String keyPrefix;

    /** 加密存储的 API Key */
    private String apiKeyEncrypted;

    /** 可用模型列表，JSON 格式 */
    private String modelsAllowed;

    /** 月度 Token 配额 */
    private Long monthlyQuotaTokens;

    /** 本月已使用的 Token 数量 */
    private Long usedTokensMonth;

    /** 每分钟请求数限制（RPM） */
    private Integer rateLimitRpm;

    /** 每分钟 Token 数限制（TPM） */
    private Integer rateLimitTpm;

    /** 代理端点URL */
    private String proxyEndpoint;

    /** 状态：ACTIVE（启用）/ DISABLED（禁用） */
    private String status;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getProviderId() { return providerId; }
    public void setProviderId(Long providerId) { this.providerId = providerId; }
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public String getKeyPrefix() { return keyPrefix; }
    public void setKeyPrefix(String keyPrefix) { this.keyPrefix = keyPrefix; }
    public String getApiKeyEncrypted() { return apiKeyEncrypted; }
    public void setApiKeyEncrypted(String apiKeyEncrypted) { this.apiKeyEncrypted = apiKeyEncrypted; }
    public String getModelsAllowed() { return modelsAllowed; }
    public void setModelsAllowed(String modelsAllowed) { this.modelsAllowed = modelsAllowed; }
    public Long getMonthlyQuotaTokens() { return monthlyQuotaTokens; }
    public void setMonthlyQuotaTokens(Long monthlyQuotaTokens) { this.monthlyQuotaTokens = monthlyQuotaTokens; }
    public Long getUsedTokensMonth() { return usedTokensMonth; }
    public void setUsedTokensMonth(Long usedTokensMonth) { this.usedTokensMonth = usedTokensMonth; }
    public Integer getRateLimitRpm() { return rateLimitRpm; }
    public void setRateLimitRpm(Integer rateLimitRpm) { this.rateLimitRpm = rateLimitRpm; }
    public Integer getRateLimitTpm() { return rateLimitTpm; }
    public void setRateLimitTpm(Integer rateLimitTpm) { this.rateLimitTpm = rateLimitTpm; }
    public String getProxyEndpoint() { return proxyEndpoint; }
    public void setProxyEndpoint(String proxyEndpoint) { this.proxyEndpoint = proxyEndpoint; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
