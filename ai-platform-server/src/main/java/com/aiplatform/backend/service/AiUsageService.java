package com.aiplatform.backend.service;

import com.aiplatform.backend.common.dto.PageResponse;
import com.aiplatform.backend.dto.AiUsageEventResponse;
import com.aiplatform.backend.dto.CreateMemberAiQuotaRequest;
import com.aiplatform.backend.dto.MemberProjectQuotaUpsertRequest;
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
    private final ProjectMemberService projectMemberService;

    /**
     * 构造函数，注入所需的数据访问层依赖。
     *
     * @param memberAiQuotaMapper 成员配额 Mapper
     * @param aiUsageEventMapper  AI 用量事件 Mapper
     * @param projectMemberService 项目成员（解析 memberId → userId）
     */
    public AiUsageService(MemberAiQuotaMapper memberAiQuotaMapper,
                          AiUsageEventMapper aiUsageEventMapper,
                          ProjectMemberService projectMemberService) {
        this.memberAiQuotaMapper = memberAiQuotaMapper;
        this.aiUsageEventMapper = aiUsageEventMapper;
        this.projectMemberService = projectMemberService;
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
        return listUsageEvents(userId, projectId, null, null, null, null, page, size);
    }

    /**
     * 分页查询 AI 用量明细，支持来源类型、状态与时间范围过滤。
     */
    public PageResponse<AiUsageEventResponse> listUsageEvents(Long userId, Long projectId,
                                                              String sourceType, String status,
                                                              java.time.LocalDateTime occurredAfter,
                                                              java.time.LocalDateTime occurredBefore,
                                                              int page, int size) {
        var query = Wrappers.<AiUsageEvent>lambdaQuery();
        if (userId != null) {
            query.eq(AiUsageEvent::getUserId, userId);
        }
        if (projectId != null) {
            query.eq(AiUsageEvent::getProjectId, projectId);
        }
        if (sourceType != null && !sourceType.isBlank()) {
            query.eq(AiUsageEvent::getSourceType, sourceType);
        }
        if (status != null && !status.isBlank()) {
            query.eq(AiUsageEvent::getStatus, status);
        }
        if (occurredAfter != null) {
            query.ge(AiUsageEvent::getOccurredAt, occurredAfter);
        }
        if (occurredBefore != null) {
            query.le(AiUsageEvent::getOccurredAt, occurredBefore);
        }
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

    /**
     * 按项目成员设置配额：强制使用路径中的 projectId，并从 memberId 解析 userId。
     *
     * <p>与库表唯一键 {@code (user_id, project_id, quota_type)} 对齐：已存在则更新上限与周期。</p>
     */
    public MemberAiQuota upsertQuotaForProjectMember(Long projectId, Long memberId,
                                                       MemberProjectQuotaUpsertRequest request) {
        var member = projectMemberService.getByProjectAndId(projectId, memberId);
        Long userId = member.getUserId();
        String quotaType = request.quotaType();
        Long quotaLimit = request.quotaLimit();
        String resetCycle = request.resetCycle() != null ? request.resetCycle() : "MONTHLY";

        MemberAiQuota existing = memberAiQuotaMapper.selectOne(
                Wrappers.<MemberAiQuota>lambdaQuery()
                        .eq(MemberAiQuota::getUserId, userId)
                        .eq(MemberAiQuota::getProjectId, projectId)
                        .eq(MemberAiQuota::getQuotaType, quotaType)
                        .last("LIMIT 1"));
        if (existing != null) {
            existing.setQuotaLimit(quotaLimit);
            existing.setResetCycle(resetCycle);
            existing.setStatus("ACTIVE");
            memberAiQuotaMapper.updateById(existing);
            return existing;
        }
        MemberAiQuota quota = new MemberAiQuota();
        quota.setUserId(userId);
        quota.setProjectId(projectId);
        quota.setQuotaType(quotaType);
        quota.setQuotaLimit(quotaLimit);
        quota.setUsedAmount(0L);
        quota.setResetCycle(resetCycle);
        quota.setStatus("ACTIVE");
        memberAiQuotaMapper.insert(quota);
        return quota;
    }
}
