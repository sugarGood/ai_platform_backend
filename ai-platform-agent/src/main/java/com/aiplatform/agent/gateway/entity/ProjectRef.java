package com.aiplatform.agent.gateway.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 项目引用实体（网关只读视图）。
 *
 * <p>映射 {@code projects} 表，网关通过该实体读取项目的 Token 配额信息，
 * 用于双池配额校验中的项目池检查。</p>
 */
@TableName("projects")
public class ProjectRef {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String code;

    /** 项目月度 Token 配额上限，0 表示不限制 */
    private Long monthlyTokenQuota;

    /** 项目当月已消耗 Token 数 */
    private Long usedTokensThisMonth;

    /** 超配额策略：BLOCK / ALLOW_WITH_ALERT / DOWNGRADE_MODEL */
    private String overQuotaStrategy;

    private String status;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public Long getMonthlyTokenQuota() { return monthlyTokenQuota; }
    public void setMonthlyTokenQuota(Long monthlyTokenQuota) { this.monthlyTokenQuota = monthlyTokenQuota; }
    public Long getUsedTokensThisMonth() { return usedTokensThisMonth; }
    public void setUsedTokensThisMonth(Long usedTokensThisMonth) { this.usedTokensThisMonth = usedTokensThisMonth; }
    public String getOverQuotaStrategy() { return overQuotaStrategy; }
    public void setOverQuotaStrategy(String overQuotaStrategy) { this.overQuotaStrategy = overQuotaStrategy; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
