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
 * 平台凭证实体，对应 {@code platform_credentials} 表。
 *
 * <h3>凭证模型：一人一证，多项目共用</h3>
 * <p>每名成员在平台拥有唯一一张凭证，邀请时自动生成。
 * 凭证跨项目复用——成员加入任意项目后，均使用同一张凭证发起 AI 调用，
 * 无需为不同项目切换或重新申请凭证。</p>
 *
 * <h3>双池配额模型</h3>
 * <p>每次 AI 调用同时扣减两个 Token 池：
 * <ol>
 *   <li><b>个人月度配额</b>（本表 {@code monthly_token_quota}）：
 *       控制单人在整个平台当月的累计消耗上限。</li>
 *   <li><b>项目 Token 池</b>（{@code project_token_quotas} 表）：
 *       控制某项目内所有成员当月的累计消耗上限。</li>
 * </ol>
 * 任意一个池触达告警阈值均产生告警；触达上限则按 {@code over_quota_strategy} 处理。</p>
 *
 * <h3>安全设计</h3>
 * <p>密钥以 SHA-256 哈希存储，仅在创建时返回一次明文。
 * 上游真实 API Key（{@code provider_api_keys}）永不下发给成员，
 * 由平台代理层透明转发。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("platform_credentials")
public class PlatformCredential {

    /** 主键 ID，自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 所属用户 ID，唯一索引。
     * 每名用户有且仅有一张凭证（uk_platform_credentials_user）。
     */
    private Long userId;

    /** 凭证展示名称，默认为用户姓名 + "的凭证" */
    private String name;

    /** 密钥前缀，用于界面脱敏展示，如 {@code plt_zhang3_8f2a****} */
    private String keyPrefix;

    /** 密钥 SHA-256 哈希值，不存储明文 */
    private String keyHash;

    /**
     * 凭证类型。
     * <ul>
     *   <li>{@code PERSONAL}：员工个人凭证，系统自动生成，有效期 30/90 天</li>
     *   <li>{@code SERVICE_ACCOUNT}：服务账号凭证，绑定 CI/CD 或 Agent，需审批</li>
     *   <li>{@code TEMP}：临时凭证，24 小时自动过期，权限受限，无需审批</li>
     * </ul>
     */
    private String credentialType;

    // ---------------------------------------------------------------
    // 个人月度 Token 配额（双池之一）
    // ---------------------------------------------------------------

    /**
     * 个人月度 Token 上限（Token 数）。
     * 0 表示不限制。由平台管理员按 job_type 默认策略分配，可单独调整。
     * 参考默认值：Developer=200K，QA=100K，PM=100K，Admin=300K，Guest=10K。
     */
    private Long monthlyTokenQuota;

    /**
     * 个人当月已消耗 Token 数（跨所有项目累计）。
     * 每次 AI 调用后由网关原子递增；每月 1 日 00:00 重置为 0。
     */
    private Long usedTokensThisMonth;

    /**
     * 告警阈值百分比（0-100）。
     * 当 {@code usedTokensThisMonth / monthlyTokenQuota >= alertThresholdPct / 100} 时触发告警。
     * 默认 80，即使用率达 80% 时告警。
     */
    private Integer alertThresholdPct;

    /**
     * 超配额策略。
     * <ul>
     *   <li>{@code BLOCK}：直接拒绝请求（默认）</li>
     *   <li>{@code ALLOW_WITH_ALERT}：放行并告警</li>
     *   <li>{@code DOWNGRADE_MODEL}：自动切换至低成本模型</li>
     * </ul>
     */
    private String overQuotaStrategy;

    /** 配额周期重置时间，记录最近一次重置的时间戳 */
    private LocalDateTime lastQuotaResetAt;

    // ---------------------------------------------------------------
    // 凭证生命周期
    // ---------------------------------------------------------------

    /**
     * 凭证过期时间。
     * PERSONAL 凭证默认 90 天；SERVICE_ACCOUNT 自定义；TEMP 凭证固定 24 小时。
     */
    private LocalDateTime expiresAt;

    /** 最后使用时间 */
    private LocalDateTime lastUsedAt;

    /** 最后使用 IP */
    private String lastUsedIp;

    /** 吊销时间，非空表示已吊销 */
    private LocalDateTime revokedAt;

    /** 吊销原因 */
    private String revokeReason;

    /**
     * 凭证状态。
     * <ul>
     *   <li>{@code ACTIVE}：正常可用</li>
     *   <li>{@code DISABLED}：临时停用</li>
     *   <li>{@code REVOKED}：已吊销，不可恢复</li>
     *   <li>{@code EXPIRED}：已自然过期</li>
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
