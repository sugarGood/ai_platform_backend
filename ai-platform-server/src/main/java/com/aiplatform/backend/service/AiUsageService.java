package com.aiplatform.backend.service;

import com.aiplatform.backend.common.dto.PageResponse;
import com.aiplatform.backend.dto.AiUsageEventResponse;
import com.aiplatform.backend.dto.CreateMemberAiQuotaRequest;
import com.aiplatform.backend.dto.MemberAiQuotaResponse;
import com.aiplatform.backend.entity.AiUsageEvent;
import com.aiplatform.backend.entity.MemberAiQuota;
import com.aiplatform.backend.mapper.AiUsageEventMapper;
import com.aiplatform.backend.mapper.MemberAiQuotaMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * AI 用量与配额业务服务。
 *
 * <p>提供成员 AI 配额的创建和查询，以及 AI 用量明细的分页查询等核心业务逻辑。</p>
 */
@Service
public class AiUsageService {

    private final MemberAiQuotaMapper memberAiQuotaMapper;
    private final AiUsageEventMapper aiUsageEventMapper;

    /**
     * 构造函数，注入所需的数据访问层依赖。
     *
     * @param memberAiQuotaMapper 成员配额 Mapper
     * @param aiUsageEventMapper  AI 用量事件 Mapper
     */
    public AiUsageService(MemberAiQuotaMapper memberAiQuotaMapper, AiUsageEventMapper aiUsageEventMapper) {
        this.memberAiQuotaMapper = memberAiQuotaMapper;
        this.aiUsageEventMapper = aiUsageEventMapper;
    }

    // ==================== 配额管理 ====================

    /**
     * 创建成员 AI 配额。
     *
     * <p>为指定用户创建新的配额记录，初始已用量为 0，默认重置周期为 MONTHLY。</p>
     *
     * @param request 创建配额请求
     * @return 新创建的配额实体
     */
    public MemberAiQuota createQuota(CreateMemberAiQuotaRequest request) {
        MemberAiQuota quota = new MemberAiQuota();
        quota.setUserId(request.userId());
        quota.setProjectId(request.projectId());
        quota.setQuotaType(request.quotaType());
        quota.setQuotaLimit(request.quotaLimit());
        quota.setUsedAmount(0L);
        quota.setResetCycle(request.resetCycle() != null ? request.resetCycle() : "MONTHLY");
        quota.setStatus("ACTIVE");
        memberAiQuotaMapper.insert(quota);
        return quota;
    }

    /**
     * 根据用户ID查询配额列表。
     *
     * @param userId 用户ID
     * @return 该用户的配额列表
     */
    public List<MemberAiQuota> listQuotasByUserId(Long userId) {
        return memberAiQuotaMapper.selectList(Wrappers.<MemberAiQuota>lambdaQuery()
                .eq(MemberAiQuota::getUserId, userId).orderByAsc(MemberAiQuota::getId));
    }

    /**
     * 根据项目ID查询配额列表。
     *
     * @param projectId 项目ID
     * @return 该项目下的配额列表
     */
    public List<MemberAiQuota> listQuotasByProjectId(Long projectId) {
        return memberAiQuotaMapper.selectList(Wrappers.<MemberAiQuota>lambdaQuery()
                .eq(MemberAiQuota::getProjectId, projectId).orderByAsc(MemberAiQuota::getId));
    }

    // ==================== 用量查询 ====================

    /**
     * 分页查询 AI 用量明细。
     *
     * <p>支持按用户ID和项目ID进行过滤，结果按ID降序排列（最新优先）。</p>
     *
     * @param userId    可选的用户ID过滤条件
     * @param projectId 可选的项目ID过滤条件
     * @param page      页码（从1开始）
     * @param size      每页大小
     * @return 分页后的 AI 用量事件响应
     */
    public PageResponse<AiUsageEventResponse> listUsageEvents(Long userId, Long projectId, int page, int size) {
        var query = Wrappers.<AiUsageEvent>lambdaQuery();
        if (userId != null) query.eq(AiUsageEvent::getUserId, userId);
        if (projectId != null) query.eq(AiUsageEvent::getProjectId, projectId);
        query.orderByDesc(AiUsageEvent::getId);
        Page<AiUsageEvent> result = aiUsageEventMapper.selectPage(new Page<>(page, size), query);
        return PageResponse.from(result, AiUsageEventResponse::from);
    }

    /** 查询我的用量概览（当月总 Token、请求数、费用）。 */
    public java.util.Map<String, Object> myUsageSummary(Long userId) {
        var query = Wrappers.<AiUsageEvent>lambdaQuery()
                .eq(AiUsageEvent::getUserId, userId)
                .ge(AiUsageEvent::getOccurredAt,
                        java.time.LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0));
        var events = aiUsageEventMapper.selectList(query);
        long totalTokens = events.stream().mapToLong(e -> e.getTotalTokens() != null ? e.getTotalTokens() : 0).sum();
        long totalRequests = events.size();
        java.math.BigDecimal totalCost = events.stream()
                .map(e -> e.getCostAmount() != null ? e.getCostAmount() : java.math.BigDecimal.ZERO)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
        return java.util.Map.of(
                "userId", userId,
                "totalTokens", totalTokens,
                "totalRequests", totalRequests,
                "totalCost", totalCost,
                "period", "CURRENT_MONTH"
        );
    }

    /** 查询项目用量统计摘要。 */
    public java.util.Map<String, Object> projectUsageSummary(Long projectId) {
        var query = Wrappers.<AiUsageEvent>lambdaQuery()
                .eq(AiUsageEvent::getProjectId, projectId)
                .ge(AiUsageEvent::getOccurredAt,
                        java.time.LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0));
        var events = aiUsageEventMapper.selectList(query);
        long totalTokens = events.stream().mapToLong(e -> e.getTotalTokens() != null ? e.getTotalTokens() : 0).sum();
        long totalRequests = events.size();
        java.math.BigDecimal totalCost = events.stream()
                .map(e -> e.getCostAmount() != null ? e.getCostAmount() : java.math.BigDecimal.ZERO)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
        return java.util.Map.of(
                "projectId", projectId,
                "totalTokens", totalTokens,
                "totalRequests", totalRequests,
                "totalCost", totalCost,
                "period", "CURRENT_MONTH"
        );
    }

    /** 查询平台级用量看板（所有项目汇总）。 */
    public java.util.Map<String, Object> platformDashboard() {
        var query = Wrappers.<AiUsageEvent>lambdaQuery()
                .ge(AiUsageEvent::getOccurredAt,
                        java.time.LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0));
        var events = aiUsageEventMapper.selectList(query);
        long totalTokens = events.stream().mapToLong(e -> e.getTotalTokens() != null ? e.getTotalTokens() : 0).sum();
        long totalRequests = events.size();
        java.math.BigDecimal totalCost = events.stream()
                .map(e -> e.getCostAmount() != null ? e.getCostAmount() : java.math.BigDecimal.ZERO)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
        long activeUsers = events.stream()
                .filter(e -> e.getUserId() != null)
                .map(AiUsageEvent::getUserId).distinct().count();
        return java.util.Map.of(
                "totalTokens", totalTokens,
                "totalRequests", totalRequests,
                "totalCost", totalCost,
                "activeUsers", activeUsers,
                "period", "CURRENT_MONTH"
        );
    }

    /** 更新成员配额。 */
    public MemberAiQuota updateQuota(Long quotaId, CreateMemberAiQuotaRequest request) {
        MemberAiQuota quota = memberAiQuotaMapper.selectById(quotaId);
        if (quota == null) throw new RuntimeException("Quota not found: " + quotaId);
        if (request.quotaLimit() != null) quota.setQuotaLimit(request.quotaLimit());
        if (request.resetCycle() != null) quota.setResetCycle(request.resetCycle());
        memberAiQuotaMapper.updateById(quota);
        return quota;
    }
}
