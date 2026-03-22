package com.aiplatform.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * AI 用量明细实体，对应 {@code ai_usage_events} 表。
 *
 * <h3>双池扣减记录</h3>
 * <p>每条记录同时携带 {@code credentialId}（个人池来源）和 {@code projectId}（项目池来源），
 * 网关在写入本记录的同时原子递增两个池的已用量：
 * <ul>
 *   <li>{@code platform_credentials.used_tokens_this_month += totalTokens}（个人池）</li>
 *   <li>{@code projects.used_tokens_this_month += totalTokens}（项目池）</li>
 * </ul>
 * 任意一个池超额均会在本记录的 {@code quotaCheckResult} 中留痕。</p>
 *
 * <p>用于用量统计、费用核算和审计追踪。</p>
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("ai_usage_events")
public class AiUsageEvent {

    /**
     * 主键 ID，自增
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    // ---------------------------------------------------------------
    // 双池关联（核心外键）
    // ---------------------------------------------------------------

    /**
     * 发起调用的平台凭证 ID（关联 {@code platform_credentials.id}）。
     * 个人月度配额的扣减依据。
     * 通过凭证可反查调用用户：{@code credential -> user}。
     */
    private Long credentialId;

    /**
     * 本次调用所属项目 ID（关联 {@code projects.id}）。
     * 项目月度 Token 池的扣减依据。
     * 同一个凭证在不同项目中的消耗分别计入对应项目的池。
     */
    private Long projectId;

    /**
     * 调用用户 ID（冗余字段，方便直接按用户查询，无需 JOIN credential）。
     * 与 {@code credentialId} 对应的 {@code platform_credentials.user_id}。
     */
    private Long userId;

    // ---------------------------------------------------------------
    // AI 调用来源
    // ---------------------------------------------------------------

    /**
     * 供应商 ID（关联 {@code ai_providers.id}）
     */
    private Long providerId;

    /**
     * 上游 API Key ID（关联 {@code provider_api_keys.id}），用于成本核算
     */
    private Long providerApiKeyId;

    /**
     * 使用的模型 ID（关联 {@code ai_models.id}）
     */
    private Long modelId;

    /**
     * 客户端应用 ID（如 Cursor、Claude Code 等，关联 {@code client_apps.id}）
     */
    private Long clientAppId;

    /**
     * 触发的技能 ID（关联 {@code skills.id}），无技能触发时为 NULL
     */
    private Long skillId;

    // ---------------------------------------------------------------
    // 请求元数据
    // ---------------------------------------------------------------

    /**
     * 用量来源类型。
     * 例：{@code MCP_TOOL}、{@code DIRECT_API}、{@code SKILL_INVOKE}、{@code AGENT_WORKFLOW}
     */
    private String sourceType;

    /**
     * 请求模式。
     * 例：{@code CHAT}、{@code CODE}、{@code EMBEDDING}、{@code MCP_TOOL}
     */
    private String requestMode;

    /**
     * 请求追踪 ID，用于跨服务链路追踪
     */
    private String requestId;

    /**
     * 会话 ID，用于关联多轮对话
     */
    private String conversationId;

    // ---------------------------------------------------------------
    // Token 消耗
    // ---------------------------------------------------------------

    /**
     * 输入 Token 数（Prompt Tokens）
     */
    private Long inputTokens;

    /**
     * 输出 Token 数（Completion Tokens）
     */
    private Long outputTokens;

    /**
     * 总 Token 数 = inputTokens + outputTokens，双池扣减的实际数值
     */
    private Long totalTokens;

    /**
     * 本次调用估算费用（USD），按模型单价计算
     */
    private BigDecimal costAmount;

    // ---------------------------------------------------------------
    // 配额检查结果
    // ---------------------------------------------------------------

    /**
     * 双池配额检查结果。
     * <ul>
     *   <li>{@code OK}：两个池均未超限</li>
     *   <li>{@code PERSONAL_QUOTA_ALERT}：个人池触达告警阈值</li>
     *   <li>{@code PROJECT_QUOTA_ALERT}：项目池触达告警阈值</li>
     *   <li>{@code PERSONAL_QUOTA_EXCEEDED}：个人池超限，按策略处理</li>
     *   <li>{@code PROJECT_QUOTA_EXCEEDED}：项目池超限，按策略处理</li>
     * </ul>
     */
    private String quotaCheckResult;

    // ---------------------------------------------------------------
    // 调用结果
    // ---------------------------------------------------------------

    /**
     * 调用状态。
     * <ul>
     *   <li>{@code SUCCESS}：成功</li>
     *   <li>{@code FAILED}：调用失败</li>
     *   <li>{@code BLOCKED_BY_QUOTA}：因超配额被拦截</li>
     *   <li>{@code BLOCKED_BY_POLICY}：因安全策略被拦截</li>
     * </ul>
     */
    private String status;

    /**
     * 错误信息，status 非 SUCCESS 时填写
     */
    private String errorMessage;

    /**
     * 代理层延迟（毫秒），不含上游响应时间
     */
    private Integer latencyMs;

    /**
     * 事件发生时间
     */
    private LocalDateTime occurredAt;

    /**
     * 记录写入时间
     */
    private LocalDateTime createdAt;


}

