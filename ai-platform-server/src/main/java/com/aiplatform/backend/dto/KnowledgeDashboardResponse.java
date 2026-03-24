package com.aiplatform.backend.dto;

import java.math.BigDecimal;

/**
 * 知识库管理页顶部统计卡片数据（与原型四块指标一一对应）。
 *
 * <p>字段语义与 {@link com.aiplatform.backend.service.KnowledgeDashboardService#getDashboard()} 内注释保持一致。</p>
 */
public record KnowledgeDashboardResponse(
        /** 全局文档总数 + 本月新增（仅 GLOBAL 活跃知识库）。 */
        GlobalDocumentsStats globalDocuments,
        /** 检索次数及环比（基于 {@code knowledge_search_logs}）。 */
        MonthlySearchStats monthlySearches,
        /** 命中率及是否达标；可能来自日志或知识库表兜底。 */
        HitRateStats hitRate,
        /** 全平台待/正在向量化的文档数。 */
        VectorizationQueueStats vectorizationQueue
) {
    /**
     * @param total         当前全局知识库下文档行数
     * @param newThisMonth  创建时间落在本自然月内的文档数
     */
    public record GlobalDocumentsStats(long total, long newThisMonth) {}

    /**
     * @param countThisMonth        本自然月检索日志条数
     * @param countLastMonth        上一自然月检索日志条数
     * @param monthOverMonthPercent 相对上月变化百分比；上月为 0 时为 null，避免除以零
     */
    public record MonthlySearchStats(long countThisMonth, long countLastMonth, BigDecimal monthOverMonthPercent) {}

    /**
     * @param percent            命中率（百分比）
     * @param meetsTarget        是否达到目标
     * @param targetPercent      目标命中率
     * @param fromSearchLogs     true 表示由本月检索日志计算；false 表示本月无日志，来自知识库表上的运行指标
     */
    public record HitRateStats(BigDecimal percent, boolean meetsTarget, BigDecimal targetPercent, boolean fromSearchLogs) {}

    /** 处于向量化流水线中的文档数（PENDING + PROCESSING）。 */
    public record VectorizationQueueStats(long processingCount) {}
}
