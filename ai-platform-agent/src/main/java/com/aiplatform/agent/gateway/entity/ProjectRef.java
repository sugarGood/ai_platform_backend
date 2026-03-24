package com.aiplatform.agent.gateway.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 项目引用实体（网关只读视图）。
 *
 * <p>映射 {@code projects} 表，网关通过该实体读取项目的 Token 配额信息，
 * 用于双池配额校验中的项目池检查。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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

}
