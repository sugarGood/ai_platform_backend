package com.aiplatform.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * 密钥轮换日志实体，对应 {@code key_rotation_logs} 表。
 *
 * <p>记录平台凭证和上游 API 密钥的轮换操作历史。</p>
 */
@TableName("key_rotation_logs")
public class KeyRotationLog {

    /** 主键 ID */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 轮换目标类型：PLATFORM_CREDENTIAL / PROVIDER_API_KEY */
    private String targetType;
    /** 目标 ID */
    private Long targetId;
    /** 目标标识（供应商名/用户名） */
    private String targetLabel;
    /** 轮换方式：MANUAL / SCHEDULED / EMERGENCY */
    private String rotationType;
    /** 旧密钥前缀 */
    private String oldKeyPrefix;
    /** 新密钥前缀 */
    private String newKeyPrefix;
    /** 轮换结果：SUCCESS / FAILED */
    private String result;
    /** 失败原因 */
    private String errorMessage;
    /** 操作人用户 ID */
    private Long operatedBy;
    /** 创建时间 */
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTargetType() { return targetType; }
    public void setTargetType(String targetType) { this.targetType = targetType; }
    public Long getTargetId() { return targetId; }
    public void setTargetId(Long targetId) { this.targetId = targetId; }
    public String getTargetLabel() { return targetLabel; }
    public void setTargetLabel(String targetLabel) { this.targetLabel = targetLabel; }
    public String getRotationType() { return rotationType; }
    public void setRotationType(String rotationType) { this.rotationType = rotationType; }
    public String getOldKeyPrefix() { return oldKeyPrefix; }
    public void setOldKeyPrefix(String oldKeyPrefix) { this.oldKeyPrefix = oldKeyPrefix; }
    public String getNewKeyPrefix() { return newKeyPrefix; }
    public void setNewKeyPrefix(String newKeyPrefix) { this.newKeyPrefix = newKeyPrefix; }
    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public Long getOperatedBy() { return operatedBy; }
    public void setOperatedBy(Long operatedBy) { this.operatedBy = operatedBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
