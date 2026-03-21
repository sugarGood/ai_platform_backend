package com.aiplatform.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * 平台凭证实体，对应 {@code platform_credentials} 表。
 *
 * <p>管理平台成员接入 AI 工具的统一凭证，支持个人凭证、服务账号和临时凭证等类型。
 * 密钥以 SHA-256 哈希存储，仅在创建时返回一次明文。</p>
 */
@TableName("platform_credentials")
public class PlatformCredential {

    /** 主键 ID */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 所属用户 ID */
    private Long userId;
    /** 凭证类型：PERSONAL / SERVICE_ACCOUNT / TEMPORARY */
    private String credentialType;
    /** 密钥 SHA-256 哈希值，不存储明文 */
    private String keyHash;
    /** 密钥前缀，用于界面脱敏展示 */
    private String keyPrefix;
    /** 凭证名称（服务账号场景使用） */
    private String name;
    /** 绑定项目 ID（服务账号可绑定单项目） */
    private Long boundProjectId;
    /** 状态：ACTIVE / REVOKED / EXPIRED */
    private String status;
    /** 过期时间 */
    private LocalDateTime expiresAt;
    /** 最后使用时间 */
    private LocalDateTime lastUsedAt;
    /** 最后使用 IP */
    private String lastUsedIp;
    /** 吊销时间 */
    private LocalDateTime revokedAt;
    /** 吊销原因 */
    private String revokeReason;
    /** 创建时间 */
    private LocalDateTime createdAt;
    /** 最后更新时间 */
    private LocalDateTime updatedAt;

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
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    public LocalDateTime getLastUsedAt() { return lastUsedAt; }
    public void setLastUsedAt(LocalDateTime lastUsedAt) { this.lastUsedAt = lastUsedAt; }
    public String getLastUsedIp() { return lastUsedIp; }
    public void setLastUsedIp(String lastUsedIp) { this.lastUsedIp = lastUsedIp; }
    public LocalDateTime getRevokedAt() { return revokedAt; }
    public void setRevokedAt(LocalDateTime revokedAt) { this.revokedAt = revokedAt; }
    public String getRevokeReason() { return revokeReason; }
    public void setRevokeReason(String revokeReason) { this.revokeReason = revokeReason; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
