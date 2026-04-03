package com.aiplatform.backend.service;

import com.aiplatform.backend.common.dto.PageResponse;
import com.aiplatform.backend.dto.ProjectConsumptionByUserRowResponse;
import com.aiplatform.backend.dto.ProjectMemberAiAccessRequest;
import com.aiplatform.backend.dto.ProjectMemberCredentialView;
import com.aiplatform.backend.dto.ProjectMemberQuotaRowResponse;
import com.aiplatform.backend.dto.ProjectTokenDashboardConfigResponse;
import com.aiplatform.backend.dto.ProjectTokenDashboardSummaryResponse;
import com.aiplatform.backend.dto.ProjectUsageActivityQuery;
import com.aiplatform.backend.dto.ProjectUsageActivityRowResponse;
import com.aiplatform.backend.dto.TokenDashboardBatchSettleRequest;
import com.aiplatform.backend.entity.AiUsageEvent;
import com.aiplatform.backend.entity.AlertEvent;
import com.aiplatform.backend.entity.MemberAiQuota;
import com.aiplatform.backend.entity.PlatformCredential;
import com.aiplatform.backend.entity.ProjectMember;
import com.aiplatform.backend.entity.User;
import com.aiplatform.backend.mapper.AiUsageEventMapper;
import com.aiplatform.backend.mapper.AlertEventMapper;
import com.aiplatform.backend.mapper.MemberAiQuotaMapper;
import com.aiplatform.backend.mapper.ProjectMemberMapper;
import com.aiplatform.backend.mapper.UserMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 项目 Token 管理仪表盘：聚合卡片、成员配额表、按人消耗与活跃日志。
 */
@Service
public class ProjectTokenDashboardService {

    public static final long DEFAULT_SINGLE_REQUEST_TOKEN_CAP = 100_000L;
    private static final String STATUS_ACTIVE = "ACTIVE";
    private static final String QUOTA_TYPE_TOKEN = "TOKEN_QUOTA";
    private static final String DEFAULT_RESET_CYCLE = "MONTHLY";
    private static final String DEFAULT_PROJECT_ROLE = "DEVELOPER";

    private static final Map<String, Long> DEFAULT_TOKEN_QUOTA_BY_ROLE = Map.of(
            "ADMIN", 300_000L,
            "DEVELOPER", 200_000L,
            "QA", 100_000L,
            "PM", 100_000L,
            "GUEST", 10_000L
    );

    private final ProjectService projectService;
    private final ProjectMemberService projectMemberService;
    private final ProjectMemberMapper projectMemberMapper;
    private final UserMapper userMapper;
    private final PlatformCredentialService platformCredentialService;
    private final MemberAiQuotaMapper memberAiQuotaMapper;
    private final AiUsageEventMapper aiUsageEventMapper;
    private final AlertEventMapper alertEventMapper;

    public ProjectTokenDashboardService(ProjectService projectService,
                                        ProjectMemberService projectMemberService,
                                        ProjectMemberMapper projectMemberMapper,
                                        UserMapper userMapper,
                                        PlatformCredentialService platformCredentialService,
                                        MemberAiQuotaMapper memberAiQuotaMapper,
                                        AiUsageEventMapper aiUsageEventMapper,
                                        AlertEventMapper alertEventMapper) {
        this.projectService = projectService;
        this.projectMemberService = projectMemberService;
        this.projectMemberMapper = projectMemberMapper;
        this.userMapper = userMapper;
        this.platformCredentialService = platformCredentialService;
        this.memberAiQuotaMapper = memberAiQuotaMapper;
        this.aiUsageEventMapper = aiUsageEventMapper;
        this.alertEventMapper = alertEventMapper;
    }

    public ProjectTokenDashboardSummaryResponse summary(Long projectId) {
        var project = projectService.getByIdOrThrow(projectId);
        long used = project.getUsedTokensThisMonth() != null ? project.getUsedTokensThisMonth() : 0L;
        Long quota = project.getMonthlyTokenQuota();
        Long remaining = (quota != null && quota > 0) ? Math.max(0, quota - used) : null;

        List<ProjectMember> members = projectMemberMapper.selectList(
                Wrappers.<ProjectMember>lambdaQuery()
                        .eq(ProjectMember::getProjectId, projectId)
                        .orderByAsc(ProjectMember::getId));

        int withAccess = 0;
        int nearLimit = 0;
        for (ProjectMember m : members) {
            PlatformCredential c = platformCredentialService.getByUserId(m.getUserId());
            if (c != null && "ACTIVE".equals(c.getStatus())) {
                withAccess++;
            }
            if (personalPoolNearAlert(c)) {
                nearLimit++;
            }
        }

        LocalDateTime monthStart = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);

        long firing = alertEventMapper.selectCount(
                Wrappers.<AlertEvent>lambdaQuery()
                        .eq(AlertEvent::getProjectId, projectId)
                        .eq(AlertEvent::getStatus, "FIRING"));

        long sevMonth = alertEventMapper.countElevatedSeverityEventsSince(projectId, monthStart);

        return new ProjectTokenDashboardSummaryResponse(
                projectId,
                used,
                quota,
                remaining,
                members.size(),
                withAccess,
                Math.max(0, members.size() - withAccess),
                firing,
                sevMonth,
                new ProjectTokenDashboardSummaryResponse.DailyReportStub(0, 0, false),
                nearLimit
        );
    }

    public ProjectTokenDashboardConfigResponse config(Long projectId) {
        var project = projectService.getByIdOrThrow(projectId);
        long used = project.getUsedTokensThisMonth() != null ? project.getUsedTokensThisMonth() : 0L;
        Long quota = project.getMonthlyTokenQuota();
        Long remaining = (quota != null && quota > 0) ? Math.max(0, quota - used) : null;
        String cycle = project.getQuotaResetCycle() != null ? project.getQuotaResetCycle() : "MONTHLY";
        Long cap = project.getSingleRequestTokenCap();
        long effectiveCap = cap != null && cap > 0 ? cap : DEFAULT_SINGLE_REQUEST_TOKEN_CAP;
        return new ProjectTokenDashboardConfigResponse(
                project.getId(),
                project.getName(),
                quota,
                used,
                remaining,
                project.getAlertThresholdPct(),
                project.getOverQuotaStrategy(),
                cycle,
                cap,
                effectiveCap,
                project.getStatus()
        );
    }

    public List<ProjectMemberQuotaRowResponse> memberQuotaRows(Long projectId) {
        projectService.getByIdOrThrow(projectId);
        List<ProjectMember> members = projectMemberMapper.selectList(
                Wrappers.<ProjectMember>lambdaQuery()
                        .eq(ProjectMember::getProjectId, projectId)
                        .orderByAsc(ProjectMember::getId));

        List<ProjectMemberQuotaRowResponse> rows = new ArrayList<>();
        for (ProjectMember m : members) {
            User u = userMapper.selectById(m.getUserId());
            PlatformCredential c = platformCredentialService.getByUserId(m.getUserId());
            MemberAiQuota tokenRow = memberAiQuotaMapper.selectOne(
                    Wrappers.<MemberAiQuota>lambdaQuery()
                            .eq(MemberAiQuota::getUserId, m.getUserId())
                            .eq(MemberAiQuota::getProjectId, projectId)
                            .eq(MemberAiQuota::getQuotaType, "TOKEN_QUOTA")
                            .last("LIMIT 1"));

            Long pQuota = c != null ? c.getMonthlyTokenQuota() : null;
            Long pUsed = c != null && c.getUsedTokensThisMonth() != null ? c.getUsedTokensThisMonth() : 0L;
            Long pRem = (pQuota != null && pQuota > 0) ? Math.max(0, pQuota - pUsed) : null;

            boolean aiOn = c != null && "ACTIVE".equals(c.getStatus());
            rows.add(new ProjectMemberQuotaRowResponse(
                    m.getId(),
                    m.getUserId(),
                    displayName(u),
                    m.getRole(),
                    c != null ? c.getId() : null,
                    pQuota,
                    pUsed,
                    pRem,
                    tokenRow != null ? tokenRow.getQuotaLimit() : null,
                    tokenRow != null && tokenRow.getUsedAmount() != null ? tokenRow.getUsedAmount() : 0L,
                    aiOn,
                    ProjectMemberCredentialView.status(c)
            ));
        }
        return rows;
    }

    public List<ProjectConsumptionByUserRowResponse> consumptionByUser(Long projectId) {
        projectService.getByIdOrThrow(projectId);
        LocalDateTime monthStart = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        List<AiUsageEvent> events = aiUsageEventMapper.selectList(
                Wrappers.<AiUsageEvent>lambdaQuery()
                        .eq(AiUsageEvent::getProjectId, projectId)
                        .ge(AiUsageEvent::getOccurredAt, monthStart));

        Map<Long, Long> sumByUser = events.stream()
                .filter(e -> e.getUserId() != null)
                .collect(Collectors.groupingBy(
                        AiUsageEvent::getUserId,
                        Collectors.summingLong(e -> e.getTotalTokens() != null ? e.getTotalTokens() : 0L)));

        long daysInMonthSoFar = Math.max(1,
                ChronoUnit.DAYS.between(monthStart.toLocalDate(), LocalDateTime.now().toLocalDate()) + 1);

        List<ProjectConsumptionByUserRowResponse> out = new ArrayList<>();
        Set<Long> userIds = sumByUser.keySet();
        Map<Long, User> userMap = loadUsers(userIds);
        Map<Long, String> roleByUser = loadRoles(projectId, userIds);

        for (Map.Entry<Long, Long> e : sumByUser.entrySet()) {
            User u = userMap.get(e.getKey());
            long tokens = e.getValue();
            double perDay = tokens / (double) daysInMonthSoFar;
            out.add(new ProjectConsumptionByUserRowResponse(
                    e.getKey(),
                    displayName(u),
                    u != null ? u.getDepartmentId() : null,
                    roleByUser.getOrDefault(e.getKey(), ""),
                    tokens,
                    perDay
            ));
        }
        out.sort((a, b) -> Long.compare(b.tokensThisMonth(), a.tokensThisMonth()));
        return out;
    }

    public PageResponse<ProjectUsageActivityRowResponse> activityLog(
            Long projectId,
            ProjectUsageActivityQuery query) {
        projectService.getByIdOrThrow(projectId);
        var q = Wrappers.<AiUsageEvent>lambdaQuery().eq(AiUsageEvent::getProjectId, projectId);
        if (query.sourceType() != null && !query.sourceType().isBlank()) {
            q.eq(AiUsageEvent::getSourceType, query.sourceType());
        }
        if (query.status() != null && !query.status().isBlank()) {
            q.eq(AiUsageEvent::getStatus, query.status());
        }
        if (query.occurredAfter() != null) {
            q.ge(AiUsageEvent::getOccurredAt, query.occurredAfter());
        }
        if (query.occurredBefore() != null) {
            q.le(AiUsageEvent::getOccurredAt, query.occurredBefore());
        }
        q.orderByDesc(AiUsageEvent::getId);
        Page<AiUsageEvent> pg = aiUsageEventMapper.selectPage(new Page<>(query.page(), query.size()), q);
        Set<Long> userIds = pg.getRecords().stream()
                .map(AiUsageEvent::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, User> userMap = loadUsers(userIds);
        return PageResponse.from(pg, ev -> toActivityRow(ev, userMap));
    }

    @Transactional
    public int syncMemberQuotas(Long projectId) {
        var project = projectService.getByIdOrThrow(projectId);
        String cycle = defaultIfBlank(project.getQuotaResetCycle(), DEFAULT_RESET_CYCLE);
        List<ProjectMember> members = projectMemberMapper.selectList(
                Wrappers.<ProjectMember>lambdaQuery()
                        .eq(ProjectMember::getProjectId, projectId));

        int created = 0;
        for (ProjectMember m : members) {
            MemberAiQuota existing = memberAiQuotaMapper.selectOne(
                    Wrappers.<MemberAiQuota>lambdaQuery()
                            .eq(MemberAiQuota::getUserId, m.getUserId())
                            .eq(MemberAiQuota::getProjectId, projectId)
                            .eq(MemberAiQuota::getQuotaType, QUOTA_TYPE_TOKEN)
                            .last("LIMIT 1"));
            if (existing != null) {
                continue;
            }
            long limit = resolveDefaultQuotaLimit(m.getRole());
            MemberAiQuota q = new MemberAiQuota();
            q.setUserId(m.getUserId());
            q.setProjectId(projectId);
            q.setQuotaType(QUOTA_TYPE_TOKEN);
            q.setQuotaLimit(limit);
            q.setUsedAmount(0L);
            q.setResetCycle(cycle);
            q.setStatus(STATUS_ACTIVE);
            memberAiQuotaMapper.insert(q);
            created++;
        }
        return created;
    }

    @Transactional
    public void batchSettle(Long projectId, TokenDashboardBatchSettleRequest request) {
        projectService.getByIdOrThrow(projectId);
        if (Boolean.TRUE.equals(request.resetMemberAiQuotaUsed())) {
            List<MemberAiQuota> list = memberAiQuotaMapper.selectList(
                    Wrappers.<MemberAiQuota>lambdaQuery()
                            .eq(MemberAiQuota::getProjectId, projectId)
                            .eq(MemberAiQuota::getQuotaType, QUOTA_TYPE_TOKEN));
            for (MemberAiQuota q : list) {
                q.setUsedAmount(0L);
                memberAiQuotaMapper.updateById(q);
            }
        }
        if (Boolean.TRUE.equals(request.resetPersonalCredentialUsed())) {
            List<ProjectMember> members = projectMemberMapper.selectList(
                    Wrappers.<ProjectMember>lambdaQuery()
                            .eq(ProjectMember::getProjectId, projectId));
            for (ProjectMember m : members) {
                platformCredentialService.resetUsedTokensThisMonthForUser(m.getUserId());
            }
        }
    }

    @Transactional
    public void setMemberAiAccess(Long projectId, Long memberId, ProjectMemberAiAccessRequest request) {
        ProjectMember member = projectMemberService.getByProjectAndId(projectId, memberId);
        platformCredentialService.setAiAccessEnabled(member.getUserId(), request.enabled());
    }

    private static ProjectUsageActivityRowResponse toActivityRow(AiUsageEvent ev, Map<Long, User> userMap) {
        User u = ev.getUserId() != null ? userMap.get(ev.getUserId()) : null;
        String mode = ev.getRequestMode() != null ? ev.getRequestMode() : "AI";
        String src = ev.getSourceType() != null ? ev.getSourceType() : "";
        String summary = (src.isEmpty() ? "" : src + " / ") + mode;
        String typeLabel = resolveActivityTypeLabel(ev);
        return new ProjectUsageActivityRowResponse(
                ev.getId(),
                ev.getUserId(),
                displayName(u),
                ev.getProjectId(),
                ev.getTotalTokens(),
                ev.getSourceType(),
                ev.getRequestMode(),
                ev.getStatus(),
                ev.getQuotaCheckResult(),
                summary.trim(),
                typeLabel,
                ev.getCostAmount(),
                ev.getOccurredAt()
        );
    }

    private static String resolveActivityTypeLabel(AiUsageEvent ev) {
        String q = ev.getQuotaCheckResult();
        if (q != null && q.contains("ALERT")) {
            return "预警";
        }
        if ("BLOCKED_BY_QUOTA".equals(ev.getStatus()) || "BLOCKED_BY_POLICY".equals(ev.getStatus())) {
            return "拦截";
        }
        return "调用";
    }

    private static boolean personalPoolNearAlert(PlatformCredential c) {
        if (c == null || !STATUS_ACTIVE.equals(c.getStatus())) {
            return false;
        }
        Long quota = c.getMonthlyTokenQuota();
        if (quota == null || quota <= 0) {
            return false;
        }
        long used = c.getUsedTokensThisMonth() != null ? c.getUsedTokensThisMonth() : 0L;
        int pct = c.getAlertThresholdPct() != null ? c.getAlertThresholdPct() : 80;
        return used * 100 >= quota * pct;
    }

    private static String displayName(User u) {
        if (u == null) {
            return "—";
        }
        if (u.getFullName() != null && !u.getFullName().isBlank()) {
            return u.getFullName();
        }
        if (u.getUsername() != null && !u.getUsername().isBlank()) {
            return u.getUsername();
        }
        return "user-" + u.getId();
    }

    private Map<Long, User> loadUsers(Set<Long> userIds) {
        if (userIds.isEmpty()) {
            return Map.of();
        }
        List<User> users = userMapper.selectList(
                Wrappers.<User>lambdaQuery().in(User::getId, userIds));
        Map<Long, User> map = new HashMap<>();
        for (User u : users) {
            map.put(u.getId(), u);
        }
        return map;
    }

    private Map<Long, String> loadRoles(Long projectId, Set<Long> userIds) {
        if (userIds.isEmpty()) {
            return Map.of();
        }
        List<ProjectMember> members = projectMemberMapper.selectList(
                Wrappers.<ProjectMember>lambdaQuery()
                        .eq(ProjectMember::getProjectId, projectId)
                        .in(ProjectMember::getUserId, userIds));
        Map<Long, String> map = new HashMap<>();
        for (ProjectMember m : members) {
            map.putIfAbsent(m.getUserId(), m.getRole() != null ? m.getRole() : "");
        }
        return map;
    }

    private long resolveDefaultQuotaLimit(String role) {
        return DEFAULT_TOKEN_QUOTA_BY_ROLE.getOrDefault(
                defaultIfBlank(role, DEFAULT_PROJECT_ROLE),
                DEFAULT_TOKEN_QUOTA_BY_ROLE.get(DEFAULT_PROJECT_ROLE));
    }

    private String defaultIfBlank(String value, String defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? defaultValue : trimmed;
    }
}
