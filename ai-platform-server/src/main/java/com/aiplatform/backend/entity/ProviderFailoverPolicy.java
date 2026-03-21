package com.aiplatform.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * 供应商故障转移策略实体，对应 provider_failover_policies 表。
 * <p>定义当主用 API 密钥出现故障时，自动切换到备用密钥的策略规则。</p>
 */
@TableName("provider_failover_policies")
public class ProviderFailoverPolicy {

    /** 主键ID，自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 策略名称 */
    private String name;

    /** 主用 API 密钥ID，关联 provider_api_keys 表 */
    private Long primaryKeyId;

    /** 备用 API 密钥ID，关联 provider_api_keys 表 */
    private Long fallbackKeyId;

    /** 触发条件，如 ERROR_RATE（错误率）、LATENCY（延迟） */
    private String triggerCondition;

    /** 触发阈值 */
    private String triggerThreshold;

    /** 是否自动恢复到主用密钥 */
    private Boolean autoRecovery;

    /** 状态：ACTIVE（启用）/ DISABLED（禁用） */
    private String status;

    /** 上次触发时间 */
    private LocalDateTime lastTriggeredAt;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Long getPrimaryKeyId() { return primaryKeyId; }
    public void setPrimaryKeyId(Long primaryKeyId) { this.primaryKeyId = primaryKeyId; }
    public Long getFallbackKeyId() { return fallbackKeyId; }
    public void setFallbackKeyId(Long fallbackKeyId) { this.fallbackKeyId = fallbackKeyId; }
    public String getTriggerCondition() { return triggerCondition; }
    public void setTriggerCondition(String triggerCondition) { this.triggerCondition = triggerCondition; }
    public String getTriggerThreshold() { return triggerThreshold; }
    public void setTriggerThreshold(String triggerThreshold) { this.triggerThreshold = triggerThreshold; }
    public Boolean getAutoRecovery() { return autoRecovery; }
    public void setAutoRecovery(Boolean autoRecovery) { this.autoRecovery = autoRecovery; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getLastTriggeredAt() { return lastTriggeredAt; }
    public void setLastTriggeredAt(LocalDateTime lastTriggeredAt) { this.lastTriggeredAt = lastTriggeredAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
