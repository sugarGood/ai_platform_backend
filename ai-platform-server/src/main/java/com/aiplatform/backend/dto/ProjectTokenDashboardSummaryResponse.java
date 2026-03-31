package com.aiplatform.backend.dto;

/**
 * 项目 Token 仪表盘顶部卡片聚合数据。
 */
public record ProjectTokenDashboardSummaryResponse(
        Long projectId,
        /** 项目池当月已消耗 Token（与 projects.used_tokens_this_month 一致） */
        Long projectUsedTokensThisMonth,
        /** 项目池月度上限 */
        Long projectMonthlyTokenQuota,
        /** 剩余额度；无上限或额度为 0 时为 null */
        Long projectRemainingTokens,
        /** 项目成员总数 */
        int memberTotal,
        /** 已具备可用平台凭证、可发起 AI 调用的成员数 */
        int membersWithAiAccess,
        /** 未开启 AI 权限（无凭证或凭证非 ACTIVE）的成员数 */
        int membersWithoutAiAccess,
        /** 当前未关闭的告警事件数（FIRING） */
        long openAlertCount,
        /** 本月按关联规则 severity 为 CRITICAL/HIGH/MEDIUM 的告警事件数（不含 LOW；库表无事件级 severity） */
        long severityAlertsThisMonth,
        /** 日报占位：尚无独立日报表时固定返回 0 */
        DailyReportStub dailyReport,
        /** 个人池已达告警阈值（≥80% 或项目配置的阈值）的成员数量 */
        int membersPersonalQuotaNearLimit
) {
    public record DailyReportStub(int submittedYesterday, int submitterCount, boolean moduleAvailable) {
    }
}
