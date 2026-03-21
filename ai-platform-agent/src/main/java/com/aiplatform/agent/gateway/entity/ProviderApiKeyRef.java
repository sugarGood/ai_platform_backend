package com.aiplatform.agent.gateway.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 上游 API 密钥引用实体（网关只读视图）。
 *
 * <p>映射 {@code provider_api_keys} 表，存储平台向上游 AI 供应商发起请求时
 * 所使用的 API 密钥。密钥以加密形式存储，网关在调用时解密使用。</p>
 */
@TableName("provider_api_keys")
public class ProviderApiKeyRef {

    /** 主键 ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属供应商 ID */
    private Long providerId;

    /** 密钥标识名称，便于管理员区分多个密钥 */
    private String label;

    /** 加密存储的 API Key，调用上游时解密使用 */
    private String apiKeyEncrypted;

    /** 密钥状态（如 ACTIVE、DISABLED） */
    private String status;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getProviderId() { return providerId; }
    public void setProviderId(Long providerId) { this.providerId = providerId; }
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public String getApiKeyEncrypted() { return apiKeyEncrypted; }
    public void setApiKeyEncrypted(String apiKeyEncrypted) { this.apiKeyEncrypted = apiKeyEncrypted; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
