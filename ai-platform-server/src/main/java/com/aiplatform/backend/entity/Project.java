package com.aiplatform.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 项目实体，对应数据库 {@code projects} 表。
 *
 * <h3>项目 Token 池（双池配额之二）</h3>
 * <p>项目拥有独立的月度 Token 池，项目内所有成员的消耗均计入该池。
 * 每次 AI 调用同时扣减：
 * <ol>
 *   <li>调用成员的 {@code platform_credentials.used_tokens_this_month}（个人池）</li>
 *   <li>本项目的 {@code used_tokens_this_month}（项目池）</li>
 * </ol>
 * 任意一个池触达告警阈值均产生告警；触达上限则按 {@code over_quota_strategy} 处理。
 * 每月 1 日 00:00 重置项目已用量。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("projects")
public class Project {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 项目名称 */
    private String name;

    /** 项目编码，全局唯一标识，如 {@code mall-system} */
    private String code;

    /** 项目描述 */
    private String description;

    /** 项目图标（emoji 或 icon name） */
    private String icon;

    /**
     * 项目类型。
     * <ul>
     *   <li>{@code PRODUCT}：对外产品型，默认池 500K Token/月</li>
     *   <li>{@code PLATFORM}：技术中台型，默认池 800K Token/月</li>
     *   <li>{@code DATA}：数据产品型，默认池 300K Token/月</li>
     *   <li>{@code OTHER}：内部系统型，默认池 300K Token/月</li>
     * </ul>
     */
    private String projectType;

    /** 创建人用户 ID */
    private Long createdBy;

    /** 项目负责人用户 ID */
    private Long ownerUserId;

    // ---------------------------------------------------------------
    // 项目 Token 池配额（双池配额之二）
    // ---------------------------------------------------------------

    /**
     * 项目月度 Token 池上限（Token 数）。
     * 0 表示不限制。由平台管理员按项目类型分配，项目 Admin 可申请临时加额。
     */
    private Long monthlyTokenQuota;

    /**
     * 项目当月已消耗 Token 数（项目内所有成员消耗之和）。
     * 每次 AI 调用后由网关原子递增；每月 1 日 00:00 重置为 0。
     */
    private Long usedTokensThisMonth;

    /**
     * 项目池告警阈值百分比（0-100）。
     * 当 {@code usedTokensThisMonth / monthlyTokenQuota >= alertThresholdPct / 100} 时
     * 向项目 Admin 及平台管理员发送告警通知。默认 80。
     */
    private Integer alertThresholdPct;

    /**
     * 项目池超配额策略。
     * <ul>
     *   <li>{@code BLOCK}：拒绝后续请求，直至下月重置或管理员加额</li>
     *   <li>{@code ALLOW_WITH_ALERT}：放行并持续告警</li>
     *   <li>{@code DOWNGRADE_MODEL}：自动切换至低成本模型</li>
     * </ul>
     */
    private String overQuotaStrategy;

    /** 项目池配额最近一次重置时间 */
    private LocalDateTime lastQuotaResetAt;

    /**
     * 任务/展示用配额重置周期（与 {@code member_ai_quotas.reset_cycle} 语义对齐）。
     * {@code DAILY} / {@code WEEKLY} / {@code MONTHLY}，默认 {@code MONTHLY}。
     */
    private String quotaResetCycle;

    /**
     * 单次请求 Token 上限；{@code null} 表示使用平台默认（例如 100K）。
     */
    private Long singleRequestTokenCap;

    // ---------------------------------------------------------------
    // 项目状态
    // ---------------------------------------------------------------

    /**
     * 项目状态。
     * <ul>
     *   <li>{@code ACTIVE}：进行中</li>
     *   <li>{@code ARCHIVED}：已归档</li>
     * </ul>
     */
    private String status;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 最后更新时间 */
    private LocalDateTime updatedAt;

    // ---------------------------------------------------------------
    // Getters & Setters
    // ---------------------------------------------------------------

}
