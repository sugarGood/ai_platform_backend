package com.aiplatform.backend.service;

import com.aiplatform.backend.common.dto.PageResponse;
import com.aiplatform.backend.dto.AiUsageEventQuery;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class AiUsageService {

    private static final String STATUS_ACTIVE = "ACTIVE";
    private static final String DEFAULT_RESET_CYCLE = "MONTHLY";
    private static final String CURRENT_MONTH_PERIOD = "CURRENT_MONTH";

    private final MemberAiQuotaMapper memberAiQuotaMapper;
    private final AiUsageEventMapper aiUsageEventMapper;
    private final ProjectMemberService projectMemberService;

    public AiUsageService(MemberAiQuotaMapper memberAiQuotaMapper,
                          AiUsageEventMapper aiUsageEventMapper,
                          ProjectMemberService projectMemberService) {
        this.memberAiQuotaMapper = memberAiQuotaMapper;
        this.aiUsageEventMapper = aiUsageEventMapper;
        this.projectMemberService = projectMemberService;
    }

    public MemberAiQuota createQuota(CreateMemberAiQuotaRequest request) {
        MemberAiQuota quota = new MemberAiQuota();
        quota.setUserId(request.userId());
        quota.setProjectId(request.projectId());
        quota.setQuotaType(request.quotaType());
        quota.setQuotaLimit(request.quotaLimit());
        quota.setUsedAmount(0L);
        quota.setResetCycle(request.resetCycle() != null ? request.resetCycle() : DEFAULT_RESET_CYCLE);
        quota.setStatus(STATUS_ACTIVE);
        memberAiQuotaMapper.insert(quota);
        return quota;
    }

    public List<MemberAiQuota> listQuotasByUserId(Long userId) {
        return memberAiQuotaMapper.selectList(Wrappers.<MemberAiQuota>lambdaQuery()
                .eq(MemberAiQuota::getUserId, userId)
                .orderByAsc(MemberAiQuota::getId));
    }

    public List<MemberAiQuota> listQuotasByProjectId(Long projectId) {
        return memberAiQuotaMapper.selectList(Wrappers.<MemberAiQuota>lambdaQuery()
                .eq(MemberAiQuota::getProjectId, projectId)
                .orderByAsc(MemberAiQuota::getId));
    }

    public PageResponse<AiUsageEventResponse> listUsageEvents(AiUsageEventQuery query) {
        var wrapper = Wrappers.<AiUsageEvent>lambdaQuery();
        if (query.userId() != null) {
            wrapper.eq(AiUsageEvent::getUserId, query.userId());
        }
        if (query.projectId() != null) {
            wrapper.eq(AiUsageEvent::getProjectId, query.projectId());
        }
        if (query.sourceType() != null && !query.sourceType().isBlank()) {
            wrapper.eq(AiUsageEvent::getSourceType, query.sourceType());
        }
        if (query.status() != null && !query.status().isBlank()) {
            wrapper.eq(AiUsageEvent::getStatus, query.status());
        }
        if (query.occurredAfter() != null) {
            wrapper.ge(AiUsageEvent::getOccurredAt, query.occurredAfter());
        }
        if (query.occurredBefore() != null) {
            wrapper.le(AiUsageEvent::getOccurredAt, query.occurredBefore());
        }
        wrapper.orderByDesc(AiUsageEvent::getId);
        Page<AiUsageEvent> result = aiUsageEventMapper.selectPage(new Page<>(query.page(), query.size()), wrapper);
        return PageResponse.from(result, AiUsageEventResponse::from);
    }

    public Map<String, Object> myUsageSummary(Long userId) {
        List<AiUsageEvent> events = aiUsageEventMapper.selectList(monthlyQuery()
                .eq(AiUsageEvent::getUserId, userId));
        return Map.of(
                "userId", userId,
                "totalTokens", sumTokens(events),
                "totalRequests", events.size(),
                "totalCost", sumCost(events),
                "period", CURRENT_MONTH_PERIOD
        );
    }

    public Map<String, Object> projectUsageSummary(Long projectId) {
        List<AiUsageEvent> events = aiUsageEventMapper.selectList(monthlyQuery()
                .eq(AiUsageEvent::getProjectId, projectId));
        return Map.of(
                "projectId", projectId,
                "totalTokens", sumTokens(events),
                "totalRequests", events.size(),
                "totalCost", sumCost(events),
                "period", CURRENT_MONTH_PERIOD
        );
    }

    public Map<String, Object> platformDashboard() {
        List<AiUsageEvent> events = aiUsageEventMapper.selectList(monthlyQuery());
        long activeUsers = events.stream()
                .filter(event -> event.getUserId() != null)
                .map(AiUsageEvent::getUserId)
                .distinct()
                .count();
        return Map.of(
                "totalTokens", sumTokens(events),
                "totalRequests", events.size(),
                "totalCost", sumCost(events),
                "activeUsers", activeUsers,
                "period", CURRENT_MONTH_PERIOD
        );
    }

    public MemberAiQuota updateQuota(Long quotaId, CreateMemberAiQuotaRequest request) {
        MemberAiQuota quota = memberAiQuotaMapper.selectById(quotaId);
        if (quota == null) {
            throw new RuntimeException("Quota not found: " + quotaId);
        }
        if (request.quotaLimit() != null) {
            quota.setQuotaLimit(request.quotaLimit());
        }
        if (request.resetCycle() != null) {
            quota.setResetCycle(request.resetCycle());
        }
        memberAiQuotaMapper.updateById(quota);
        return quota;
    }

    public MemberAiQuota upsertQuotaForProjectMember(Long projectId,
                                                     Long memberId,
                                                     MemberProjectQuotaUpsertRequest request) {
        var member = projectMemberService.getByProjectAndId(projectId, memberId);
        Long userId = member.getUserId();
        String resetCycle = request.resetCycle() != null ? request.resetCycle() : DEFAULT_RESET_CYCLE;

        MemberAiQuota existing = memberAiQuotaMapper.selectOne(
                Wrappers.<MemberAiQuota>lambdaQuery()
                        .eq(MemberAiQuota::getUserId, userId)
                        .eq(MemberAiQuota::getProjectId, projectId)
                        .eq(MemberAiQuota::getQuotaType, request.quotaType())
                        .last("LIMIT 1")
        );
        if (existing != null) {
            existing.setQuotaLimit(request.quotaLimit());
            existing.setResetCycle(resetCycle);
            existing.setStatus(STATUS_ACTIVE);
            memberAiQuotaMapper.updateById(existing);
            return existing;
        }

        MemberAiQuota quota = new MemberAiQuota();
        quota.setUserId(userId);
        quota.setProjectId(projectId);
        quota.setQuotaType(request.quotaType());
        quota.setQuotaLimit(request.quotaLimit());
        quota.setUsedAmount(0L);
        quota.setResetCycle(resetCycle);
        quota.setStatus(STATUS_ACTIVE);
        memberAiQuotaMapper.insert(quota);
        return quota;
    }

    private com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<AiUsageEvent> monthlyQuery() {
        return Wrappers.<AiUsageEvent>lambdaQuery()
                .ge(AiUsageEvent::getOccurredAt, currentMonthStart());
    }

    private LocalDateTime currentMonthStart() {
        return LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
    }

    private long sumTokens(List<AiUsageEvent> events) {
        return events.stream()
                .mapToLong(event -> event.getTotalTokens() != null ? event.getTotalTokens() : 0L)
                .sum();
    }

    private BigDecimal sumCost(List<AiUsageEvent> events) {
        return events.stream()
                .map(event -> event.getCostAmount() != null ? event.getCostAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
