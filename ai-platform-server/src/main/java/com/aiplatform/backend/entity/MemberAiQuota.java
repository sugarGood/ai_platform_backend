package com.aiplatform.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * 成员 AI 配额实体，对应 member_ai_quotas 表。
 *
 * <p>管理平台成员在项目或个人维度的 AI 使用配额，支持 Token、费用和请求次数等多种配额类型，
 * 并可按日、周、月周期自动重置。</p>
 */
@TableName("member_ai_quotas")
public class MemberAiQuota {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 项目ID，为 NULL 时表示个人总配额 */
    private Long projectId;

    /** 配额类型：TOKEN_QUOTA（Token 配额）/ COST_QUOTA（费用配额）/ REQUEST_QUOTA（请求次数配额） */
    private String quotaType;

    /** 配额上限 */
    private Long quotaLimit;

    /** 已使用量 */
    private Long usedAmount;

    /** 重置周期：DAILY（每日）/ WEEKLY（每周）/ MONTHLY（每月） */
    private String resetCycle;

    /** 上次重置时间 */
    private LocalDateTime lastResetAt;

    /** 状态：ACTIVE / INACTIVE */
    private String status;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 最后更新时间 */
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }
    public String getQuotaType() { return quotaType; }
    public void setQuotaType(String quotaType) { this.quotaType = quotaType; }
    public Long getQuotaLimit() { return quotaLimit; }
    public void setQuotaLimit(Long quotaLimit) { this.quotaLimit = quotaLimit; }
    public Long getUsedAmount() { return usedAmount; }
    public void setUsedAmount(Long usedAmount) { this.usedAmount = usedAmount; }
    public String getResetCycle() { return resetCycle; }
    public void setResetCycle(String resetCycle) { this.resetCycle = resetCycle; }
    public LocalDateTime getLastResetAt() { return lastResetAt; }
    public void setLastResetAt(LocalDateTime lastResetAt) { this.lastResetAt = lastResetAt; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
