package com.aiplatform.agent.gateway.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * 平台凭证引用实体（网关只读视图）。
 *
 * <p>映射 {@code platform_credentials} 表，网关模块通过该实体读取凭证信息，
 * 用于验证调用方身份。网关侧仅执行读取操作，不对凭证数据做写入变更。</p>
 */
@TableName("platform_credentials")
public class PlatformCredentialRef {

    /** 主键 ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属用户 ID */
    private Long userId;

    /** 凭证类型（如 API_KEY、ACCESS_TOKEN 等） */
    private String credentialType;

    /** 密钥的 SHA-256 哈希值，用于安全匹配 */
    private String keyHash;

    /** 密钥前缀，用于在界面上脱敏展示 */
    private String keyPrefix;

    /** 凭证名称 */
    private String name;

    /** 绑定的项目 ID，为空表示不限定项目 */
    private Long boundProjectId;

    /** 个人月度 Token 配额上限，0 表示不限制 */
    private Long monthlyTokenQuota;

    /** 个人当月已消耗 Token 数 */
    private Long usedTokensThisMonth;

    /** 超配额策略：BLOCK / ALLOW_WITH_ALERT / DOWNGRADE_MODEL */
    private String overQuotaStrategy;

    /** 凭证状态（如 ACTIVE、REVOKED） */
    private String status;

    /** 过期时间，为空表示永不过期 */
    private LocalDateTime expiresAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getCredentialType() { return credentialType; }
    public void setCredentialType(String credentialType) { this.credentialType = credentialType; }
    public String getKeyHash() { return keyHash; }
    public void setKeyHash(String keyHash) { this.keyHash = keyHash; }
    public String getKeyPrefix() { return keyPrefix; }
    public void setKeyPrefix(String keyPrefix) { this.keyPrefix = keyPrefix; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Long getBoundProjectId() { return boundProjectId; }
    public void setBoundProjectId(Long boundProjectId) { this.boundProjectId = boundProjectId; }
    public Long getMonthlyTokenQuota() { return monthlyTokenQuota; }
    public void setMonthlyTokenQuota(Long monthlyTokenQuota) { this.monthlyTokenQuota = monthlyTokenQuota; }
    public Long getUsedTokensThisMonth() { return usedTokensThisMonth; }
    public void setUsedTokensThisMonth(Long usedTokensThisMonth) { this.usedTokensThisMonth = usedTokensThisMonth; }
    public String getOverQuotaStrategy() { return overQuotaStrategy; }
    public void setOverQuotaStrategy(String overQuotaStrategy) { this.overQuotaStrategy = overQuotaStrategy; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
}
