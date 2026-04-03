package com.aiplatform.backend.service;

import com.aiplatform.backend.common.dto.PageResponse;
import com.aiplatform.backend.dto.MyUsageEventResponse;
import com.aiplatform.backend.dto.MyUsageSummaryResponse;
import com.aiplatform.backend.dto.UsageProjectDistributionRowResponse;
import com.aiplatform.backend.dto.UsageTrendPointResponse;
import com.aiplatform.backend.entity.AiUsageEvent;
import com.aiplatform.backend.mapper.AiUsageEventMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 我的用量服务。
 */
@Service
public class MyUsageService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final AiUsageEventMapper aiUsageEventMapper;

    public MyUsageService(AiUsageEventMapper aiUsageEventMapper) {
        this.aiUsageEventMapper = aiUsageEventMapper;
    }

    /**
     * 查询我的用量总览。
     */
    public MyUsageSummaryResponse getSummary(Long userId) {
        YearMonth currentMonth = YearMonth.now();
        LocalDateTime monthStart = currentMonth.atDay(1).atStartOfDay();
        LocalDateTime nextMonthStart = currentMonth.plusMonths(1).atDay(1).atStartOfDay();

        List<AiUsageEvent> events = listByUserAndTimeRange(userId, monthStart, nextMonthStart);

        long totalTokens = 0L;
        int totalRequests = events.size();
        BigDecimal totalCost = BigDecimal.ZERO;

        for (AiUsageEvent event : events) {
            totalTokens += event.getTotalTokens() == null ? 0L : event.getTotalTokens();
            if (event.getCostAmount() != null) {
                totalCost = totalCost.add(event.getCostAmount());
            }
        }

        return new MyUsageSummaryResponse(
                userId,
                totalTokens,
                totalRequests,
                totalCost,
                currentMonth.toString());
    }

    /**
     * 查询我的用量趋势。
     */
    public List<UsageTrendPointResponse> getTrend(Long userId, Integer days) {
        int safeDays = days == null || days <= 0 ? 7 : days;
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(safeDays - 1L);

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTimeExclusive = endDate.plusDays(1L).atStartOfDay();
        List<AiUsageEvent> events = listByUserAndTimeRange(userId, startDateTime, endDateTimeExclusive);

        Map<LocalDate, UsageAccumulator> grouped = new LinkedHashMap<>();
        for (int i = 0; i < safeDays; i++) {
            LocalDate date = startDate.plusDays(i);
            grouped.put(date, new UsageAccumulator());
        }

        for (AiUsageEvent event : events) {
            if (event.getOccurredAt() == null) {
                continue;
            }
            LocalDate date = event.getOccurredAt().toLocalDate();
            UsageAccumulator accumulator = grouped.get(date);
            if (accumulator == null) {
                continue;
            }
            accumulator.totalTokens += event.getTotalTokens() == null ? 0L : event.getTotalTokens();
            accumulator.totalRequests += 1;
        }

        List<UsageTrendPointResponse> result = new ArrayList<>();
        for (Map.Entry<LocalDate, UsageAccumulator> entry : grouped.entrySet()) {
            result.add(new UsageTrendPointResponse(
                    entry.getKey().format(DATE_FORMATTER),
                    entry.getValue().totalTokens,
                    entry.getValue().totalRequests));
        }
        return result;
    }

    /**
     * 查询我的请求明细。
     */
    public PageResponse<MyUsageEventResponse> getEvents(Long userId, Integer page, Integer size) {
        int safePage = page == null || page <= 0 ? 1 : page;
        int safeSize = size == null || size <= 0 ? 20 : size;

        Page<AiUsageEvent> queryPage = new Page<>(safePage, safeSize);
        Page<AiUsageEvent> resultPage = aiUsageEventMapper.selectPage(
                queryPage,
                new LambdaQueryWrapper<AiUsageEvent>()
                        .eq(AiUsageEvent::getUserId, userId)
                        .orderByDesc(AiUsageEvent::getOccurredAt)
                        .orderByDesc(AiUsageEvent::getId)
        );

        return PageResponse.from(resultPage, event -> new MyUsageEventResponse(
                event.getId(),
                event.getProjectId(),
                event.getModelId(),
                event.getRequestMode(),
                event.getRequestId(),
                event.getInputTokens(),
                event.getOutputTokens(),
                event.getTotalTokens(),
                event.getCostAmount(),
                event.getQuotaCheckResult(),
                event.getStatus(),
                event.getErrorMessage(),
                event.getLatencyMs(),
                event.getOccurredAt()));
    }

    /**
     * 查询按项目分布。
     */
    public List<UsageProjectDistributionRowResponse> getProjectDistribution(Long userId) {
        YearMonth currentMonth = YearMonth.now();
        LocalDateTime monthStart = currentMonth.atDay(1).atStartOfDay();
        LocalDateTime nextMonthStart = currentMonth.plusMonths(1).atDay(1).atStartOfDay();
        List<AiUsageEvent> events = listByUserAndTimeRange(userId, monthStart, nextMonthStart);

        Map<Long, ProjectAccumulator> grouped = new LinkedHashMap<>();
        for (AiUsageEvent event : events) {
            Long projectId = event.getProjectId();
            if (projectId == null) {
                continue;
            }
            ProjectAccumulator accumulator = grouped.computeIfAbsent(projectId, key -> new ProjectAccumulator());
            accumulator.totalTokens += event.getTotalTokens() == null ? 0L : event.getTotalTokens();
            accumulator.totalRequests += 1;
            if (event.getCostAmount() != null) {
                accumulator.totalCost = accumulator.totalCost.add(event.getCostAmount());
            }
        }

        List<UsageProjectDistributionRowResponse> result = new ArrayList<>();
        for (Map.Entry<Long, ProjectAccumulator> entry : grouped.entrySet()) {
            result.add(new UsageProjectDistributionRowResponse(
                    entry.getKey(),
                    entry.getValue().totalTokens,
                    entry.getValue().totalRequests,
                    entry.getValue().totalCost));
        }
        return result;
    }

    private List<AiUsageEvent> listByUserAndTimeRange(Long userId,
                                                       LocalDateTime startInclusive,
                                                       LocalDateTime endExclusive) {
        return aiUsageEventMapper.selectList(
                new LambdaQueryWrapper<AiUsageEvent>()
                        .eq(AiUsageEvent::getUserId, userId)
                        .ge(AiUsageEvent::getOccurredAt, startInclusive)
                        .lt(AiUsageEvent::getOccurredAt, endExclusive)
                        .orderByAsc(AiUsageEvent::getOccurredAt)
                        .orderByAsc(AiUsageEvent::getId)
        );
    }

    private static class UsageAccumulator {
        private long totalTokens;
        private int totalRequests;
    }

    private static class ProjectAccumulator {
        private long totalTokens;
        private int totalRequests;
        private BigDecimal totalCost = BigDecimal.ZERO;
    }
}
