package com.aiplatform.backend.service;

import com.aiplatform.backend.dto.KnowledgeDashboardResponse;
import com.aiplatform.backend.entity.KbDocument;
import com.aiplatform.backend.entity.KnowledgeBase;
import com.aiplatform.backend.entity.KnowledgeSearchLog;
import com.aiplatform.backend.mapper.KbDocumentMapper;
import com.aiplatform.backend.mapper.KnowledgeBaseMapper;
import com.aiplatform.backend.mapper.KnowledgeSearchLogMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;

/**
 * 知识库仪表盘统计服务。
 *
 * <p>聚合四类指标，供管理端「知识库」页顶部卡片展示：</p>
 * <ul>
 *   <li>全局文档：仅统计 {@code scope=GLOBAL} 且 {@code ACTIVE} 的知识库下的文档</li>
 *   <li>检索次数 / 命中率：优先基于 {@code knowledge_search_logs}；无本月日志时命中率回退到知识库表上的运行字段</li>
 *   <li>向量化队列：全平台文档中仍处于 {@code PENDING}/{@code PROCESSING} 的数量</li>
 * </ul>
 */
@Service
public class KnowledgeDashboardService {

    /** 命中率达标线（与原型「目标 ≥ 70%」一致，可按后续配置化需求再外提）。 */
    private static final BigDecimal DEFAULT_HIT_RATE_TARGET = BigDecimal.valueOf(70);

    private final KnowledgeBaseMapper knowledgeBaseMapper;
    private final KbDocumentMapper kbDocumentMapper;
    private final KnowledgeSearchLogMapper knowledgeSearchLogMapper;

    public KnowledgeDashboardService(KnowledgeBaseMapper knowledgeBaseMapper,
                                     KbDocumentMapper kbDocumentMapper,
                                     KnowledgeSearchLogMapper knowledgeSearchLogMapper) {
        this.knowledgeBaseMapper = knowledgeBaseMapper;
        this.kbDocumentMapper = kbDocumentMapper;
        this.knowledgeSearchLogMapper = knowledgeSearchLogMapper;
    }

    /**
     * 组装仪表盘 DTO；各指标独立计数，避免与列表接口的 N+1 查询。
     */
    public KnowledgeDashboardResponse getDashboard() {
        LocalDate today = LocalDate.now();
        // 自然月边界（使用服务器默认时区，与 DB 中 datetime 对齐依赖部署环境）
        LocalDateTime monthStart = LocalDateTime.of(today.withDayOfMonth(1), LocalTime.MIN);
        LocalDateTime lastMonthStart = LocalDateTime.of(today.withDayOfMonth(1).minusMonths(1), LocalTime.MIN);
        // 上月最后一瞬，与本月 0 点互斥，避免检索日志被重复计入两个月
        LocalDateTime lastMonthEnd = monthStart.minusNanos(1);

        // ── 卡片一：全局文档（限定 GLOBAL + ACTIVE 知识库） ─────────────────
        List<Long> globalKbIds = knowledgeBaseMapper.selectList(Wrappers.<KnowledgeBase>lambdaQuery()
                        .eq(KnowledgeBase::getScope, "GLOBAL")
                        .eq(KnowledgeBase::getStatus, "ACTIVE"))
                .stream()
                .map(KnowledgeBase::getId)
                .filter(Objects::nonNull)
                .toList();

        // 无全局库时直接为 0，避免 MyBatis in () 非法 SQL
        long globalDocTotal = globalKbIds.isEmpty()
                ? 0L
                : kbDocumentMapper.selectCount(Wrappers.<KbDocument>lambdaQuery().in(KbDocument::getKbId, globalKbIds));
        long globalDocNewThisMonth = globalKbIds.isEmpty()
                ? 0L
                : kbDocumentMapper.selectCount(Wrappers.<KbDocument>lambdaQuery()
                        .in(KbDocument::getKbId, globalKbIds)
                        .ge(KbDocument::getCreatedAt, monthStart));

        // ── 卡片二：本月 / 上月检索次数（全平台日志，不限定知识库） ─────────────
        long searchesThisMonth = knowledgeSearchLogMapper.selectCount(Wrappers.<KnowledgeSearchLog>lambdaQuery()
                .ge(KnowledgeSearchLog::getCreatedAt, monthStart));
        long searchesLastMonth = knowledgeSearchLogMapper.selectCount(Wrappers.<KnowledgeSearchLog>lambdaQuery()
                .ge(KnowledgeSearchLog::getCreatedAt, lastMonthStart)
                .le(KnowledgeSearchLog::getCreatedAt, lastMonthEnd));

        // 环比：分母为 0 时不造虚假百分比，交给前端展示「—」或隐藏箭头
        BigDecimal momPercent = null;
        if (searchesLastMonth > 0) {
            BigDecimal diff = BigDecimal.valueOf(searchesThisMonth - searchesLastMonth);
            momPercent = diff.multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(searchesLastMonth), 1, RoundingMode.HALF_UP);
        }else {
            momPercent = BigDecimal.valueOf(1);
        }

        // ── 卡片三：命中率 ───────────────────────────────────────────────────
        // 「有结果」= result_count > 0，与网关写库语义一致
        long hitsThisMonth = knowledgeSearchLogMapper.selectCount(Wrappers.<KnowledgeSearchLog>lambdaQuery()
                .ge(KnowledgeSearchLog::getCreatedAt, monthStart)
                .gt(KnowledgeSearchLog::getResultCount, 0));

        List<KnowledgeBase> globalKbs = knowledgeBaseMapper.selectList(Wrappers.<KnowledgeBase>lambdaQuery()
                .eq(KnowledgeBase::getScope, "GLOBAL")
                .eq(KnowledgeBase::getStatus, "ACTIVE"));

        // 本月有检索样本：用日志实时比例；否则用知识库表上维护的 hit_rate 均值兜底（冷启动 / 未接网关时）
        boolean fromLogs = searchesThisMonth > 0;
        BigDecimal hitRatePercent;
        if (fromLogs) {
            hitRatePercent = BigDecimal.valueOf(hitsThisMonth)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(searchesThisMonth), 2, RoundingMode.HALF_UP);
        } else {
            hitRatePercent = averageHitRateFromKnowledgeBases(globalKbs);
            if (hitRatePercent == null) {
                hitRatePercent = BigDecimal.ZERO;
            }
        }

        boolean meetsTarget = hitRatePercent.compareTo(DEFAULT_HIT_RATE_TARGET) >= 0;

        // ── 卡片四：向量化队列（全库文档，不限 GLOBAL） ─────────────────────
        long queueCount = kbDocumentMapper.selectCount(Wrappers.<KbDocument>lambdaQuery()
                .in(KbDocument::getStatus, "PENDING", "PROCESSING"));

        return new KnowledgeDashboardResponse(
                new KnowledgeDashboardResponse.GlobalDocumentsStats(globalDocTotal, globalDocNewThisMonth),
                new KnowledgeDashboardResponse.MonthlySearchStats(searchesThisMonth, searchesLastMonth, momPercent),
                new KnowledgeDashboardResponse.HitRateStats(hitRatePercent, meetsTarget, DEFAULT_HIT_RATE_TARGET, fromLogs),
                new KnowledgeDashboardResponse.VectorizationQueueStats(queueCount)
        );
    }

    /**
     * 全局知识库 {@code hit_rate} 的简单算术平均；无数据返回 null，由调用方置 0。
     */
    private static BigDecimal averageHitRateFromKnowledgeBases(List<KnowledgeBase> globalKbs) {
        List<BigDecimal> rates = globalKbs.stream()
                .map(KnowledgeBase::getHitRate)
                .filter(Objects::nonNull)
                .toList();
        if (rates.isEmpty()) {
            return null;
        }
        BigDecimal sum = rates.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return sum.divide(BigDecimal.valueOf(rates.size()), 2, RoundingMode.HALF_UP);
    }
}
