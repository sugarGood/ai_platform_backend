package com.aiplatform.backend.service;

import com.aiplatform.backend.common.dto.PageResponse;
import com.aiplatform.backend.dto.ProjectCardAiMetrics;
import com.aiplatform.backend.dto.ProjectCardResponse;
import com.aiplatform.backend.dto.ProjectDashboardQuery;
import com.aiplatform.backend.dto.ProjectCardTokenUsage;
import com.aiplatform.backend.entity.KnowledgeBase;
import com.aiplatform.backend.entity.Project;
import com.aiplatform.backend.entity.ProjectKnowledgeConfig;
import com.aiplatform.backend.entity.ProjectMember;
import com.aiplatform.backend.entity.ProjectSkill;
import com.aiplatform.backend.entity.ProjectTool;
import com.aiplatform.backend.entity.ServiceEntity;
import com.aiplatform.backend.entity.User;
import com.aiplatform.backend.mapper.KnowledgeBaseMapper;
import com.aiplatform.backend.mapper.ProjectKnowledgeConfigMapper;
import com.aiplatform.backend.mapper.ProjectMapper;
import com.aiplatform.backend.mapper.ProjectMemberMapper;
import com.aiplatform.backend.mapper.ProjectSkillMapper;
import com.aiplatform.backend.mapper.ProjectToolMapper;
import com.aiplatform.backend.mapper.ServiceEntityMapper;
import com.aiplatform.backend.mapper.UserMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 项目工作台卡片页聚合查询（单接口支撑项目网格 UI）。
 */
@Service
public class ProjectDashboardService {

    private static final int MAX_MEMBER_AVATAR_PREVIEW = 6;

    private static final Map<String, String> PROJECT_TYPE_LABELS = Map.of(
            "PRODUCT", "产品项目",
            "PLATFORM", "平台能力",
            "DATA", "数据项目",
            "OTHER", "其他"
    );

    private final ProjectMapper projectMapper;
    private final ProjectMemberMapper projectMemberMapper;
    private final ServiceEntityMapper serviceEntityMapper;
    private final ProjectSkillMapper projectSkillMapper;
    private final ProjectToolMapper projectToolMapper;
    private final KnowledgeBaseMapper knowledgeBaseMapper;
    private final ProjectKnowledgeConfigMapper projectKnowledgeConfigMapper;
    private final UserMapper userMapper;

    public ProjectDashboardService(
            ProjectMapper projectMapper,
            ProjectMemberMapper projectMemberMapper,
            ServiceEntityMapper serviceEntityMapper,
            ProjectSkillMapper projectSkillMapper,
            ProjectToolMapper projectToolMapper,
            KnowledgeBaseMapper knowledgeBaseMapper,
            ProjectKnowledgeConfigMapper projectKnowledgeConfigMapper,
            UserMapper userMapper) {
        this.projectMapper = projectMapper;
        this.projectMemberMapper = projectMemberMapper;
        this.serviceEntityMapper = serviceEntityMapper;
        this.projectSkillMapper = projectSkillMapper;
        this.projectToolMapper = projectToolMapper;
        this.knowledgeBaseMapper = knowledgeBaseMapper;
        this.projectKnowledgeConfigMapper = projectKnowledgeConfigMapper;
        this.userMapper = userMapper;
    }

    /**
     * 分页返回项目卡片数据（成员、服务、技能、工具、知识库、Token、头像预览等）。
     *
     * @param page              页码，从 1 开始
     * @param size              每页条数
     * @param includeArchived   为 true 且未指定具体 {@code status} 时包含非 ACTIVE 项目
     * @param keyword           可选；匹配名称、编码、描述、ID（字符串模糊）
     * @param status            可选；ALL/ACTIVE/ARCHIVED（及「全部状态」等同 ALL）
     * @param projectType       可选；ALL 或 PRODUCT/PLATFORM/DATA/OTHER
     */
    public PageResponse<ProjectCardResponse> listDashboard(ProjectDashboardQuery query) {
        var wrapper = Wrappers.<Project>lambdaQuery().orderByAsc(Project::getId);
        ProjectQueryFilters.applyForDashboard(
                wrapper,
                query.keyword(),
                query.status(),
                query.projectType(),
                query.includeArchived());
        Page<Project> result = projectMapper.selectPage(new Page<>(query.page(), query.size()), wrapper);
        List<Project> projects = result.getRecords();
        if (projects.isEmpty()) {
            return new PageResponse<>(List.of(), result.getTotal(), (int) result.getCurrent(), (int) result.getSize());
        }

        List<Long> projectIds = projects.stream().map(Project::getId).toList();
        Map<Long, Long> memberCounts = countGrouped(projectMemberMapper, "project_id", projectIds);
        Map<Long, Long> serviceCounts = countActiveServices(projectIds);
        Map<Long, Long> skillCounts = countGroupedActive(projectSkillMapper, ProjectSkill.class, "project_id", projectIds);
        Map<Long, Long> toolCounts = countGroupedActive(projectToolMapper, ProjectTool.class, "project_id", projectIds);
        Map<Long, Long> dedicatedKb = countDedicatedKnowledgeBases(projectIds);
        Map<Long, Long> inheritedKb = countInheritedGlobalKnowledgeBases(projectIds);
        Map<Long, List<String>> avatarPreviews = buildMemberAvatarPreviews(projectIds);

        List<ProjectCardResponse> cards = new ArrayList<>(projects.size());
        for (Project p : projects) {
            Long pid = p.getId();
            long members = memberCounts.getOrDefault(pid, 0L);
            long kb = dedicatedKb.getOrDefault(pid, 0L) + inheritedKb.getOrDefault(pid, 0L);
            var ai = new ProjectCardAiMetrics(
                    skillCounts.getOrDefault(pid, 0L).intValue(),
                    toolCounts.getOrDefault(pid, 0L).intValue(),
                    (int) kb);
            Long quota = p.getMonthlyTokenQuota();
            long used = p.getUsedTokensThisMonth() != null ? p.getUsedTokensThisMonth() : 0L;
            Long limit = (quota != null && quota > 0) ? quota : null;
            var token = new ProjectCardTokenUsage(used, limit);
            String type = p.getProjectType();
            String label = PROJECT_TYPE_LABELS.getOrDefault(type != null ? type : "", type != null ? type : "");
            cards.add(ProjectCardResponse.of(
                    p,
                    label,
                    serviceCounts.getOrDefault(pid, 0L),
                    ai,
                    token,
                    members,
                    avatarPreviews.getOrDefault(pid, List.of())));
        }
        return new PageResponse<>(cards, result.getTotal(), (int) result.getCurrent(), (int) result.getSize());
    }

    private static Map<Long, Long> countGrouped(
            ProjectMemberMapper mapper, String projectColumn, List<Long> projectIds) {
        if (projectIds.isEmpty()) {
            return Map.of();
        }
        QueryWrapper<ProjectMember> w = new QueryWrapper<>();
        w.select(projectColumn, "COUNT(*) AS c")
                .in(projectColumn, projectIds)
                .groupBy(projectColumn);
        return mapsToProjectLong(mapper.selectMaps(w), projectColumn);
    }

    private Map<Long, Long> countActiveServices(List<Long> projectIds) {
        if (projectIds.isEmpty()) {
            return Map.of();
        }
        QueryWrapper<ServiceEntity> w = new QueryWrapper<>();
        w.select("project_id", "COUNT(*) AS c")
                .in("project_id", projectIds)
                .eq("status", "ACTIVE")
                .groupBy("project_id");
        return mapsToProjectLong(serviceEntityMapper.selectMaps(w), "project_id");
    }

    private static <T> Map<Long, Long> countGroupedActive(
            com.baomidou.mybatisplus.core.mapper.BaseMapper<T> mapper,
            Class<T> entityClass,
            String projectColumn,
            List<Long> projectIds) {
        if (projectIds.isEmpty()) {
            return Map.of();
        }
        QueryWrapper<T> w = new QueryWrapper<>();
        w.select(projectColumn, "COUNT(*) AS c")
                .in(projectColumn, projectIds)
                .eq("status", "ACTIVE")
                .groupBy(projectColumn);
        return mapsToProjectLong(mapper.selectMaps(w), projectColumn);
    }

    private Map<Long, Long> countDedicatedKnowledgeBases(List<Long> projectIds) {
        if (projectIds.isEmpty()) {
            return Map.of();
        }
        QueryWrapper<KnowledgeBase> w = new QueryWrapper<>();
        w.select("project_id", "COUNT(*) AS c")
                .eq("scope", "PROJECT")
                .in("project_id", projectIds)
                .groupBy("project_id");
        return mapsToProjectLong(knowledgeBaseMapper.selectMaps(w), "project_id");
    }

    /** 仅统计指向 GLOBAL 知识库的项目配置（与知识库来源列表语义一致）。 */
    private Map<Long, Long> countInheritedGlobalKnowledgeBases(List<Long> projectIds) {
        if (projectIds.isEmpty()) {
            return Map.of();
        }
        List<ProjectKnowledgeConfig> configs = projectKnowledgeConfigMapper.selectList(
                Wrappers.<ProjectKnowledgeConfig>lambdaQuery()
                        .in(ProjectKnowledgeConfig::getProjectId, projectIds));
        if (configs.isEmpty()) {
            return Map.of();
        }
        Set<Long> kbIds = new HashSet<>();
        for (ProjectKnowledgeConfig c : configs) {
            if (c.getKbId() != null) {
                kbIds.add(c.getKbId());
            }
        }
        if (kbIds.isEmpty()) {
            return Map.of();
        }
        List<KnowledgeBase> kbs = knowledgeBaseMapper.selectList(
                Wrappers.<KnowledgeBase>lambdaQuery().in(KnowledgeBase::getId, kbIds));
        Set<Long> globalKbIds = new HashSet<>();
        for (KnowledgeBase kb : kbs) {
            if ("GLOBAL".equals(kb.getScope())) {
                globalKbIds.add(kb.getId());
            }
        }
        Map<Long, Long> out = new HashMap<>();
        for (ProjectKnowledgeConfig c : configs) {
            if (c.getKbId() != null && globalKbIds.contains(c.getKbId())) {
                Long pid = c.getProjectId();
                out.put(pid, out.getOrDefault(pid, 0L) + 1L);
            }
        }
        return out;
    }

    private Map<Long, List<String>> buildMemberAvatarPreviews(List<Long> projectIds) {
        Map<Long, List<String>> out = new HashMap<>();
        for (Long pid : projectIds) {
            out.put(pid, new ArrayList<>());
        }
        if (projectIds.isEmpty()) {
            return out;
        }
        List<ProjectMember> members = projectMemberMapper.selectList(
                Wrappers.<ProjectMember>lambdaQuery()
                        .in(ProjectMember::getProjectId, projectIds)
                        .orderByAsc(ProjectMember::getProjectId)
                        .orderByAsc(ProjectMember::getJoinedAt)
                        .orderByAsc(ProjectMember::getId));
        Map<Long, List<Long>> previewUserIds = new HashMap<>();
        for (Long pid : projectIds) {
            previewUserIds.put(pid, new ArrayList<>());
        }
        for (ProjectMember m : members) {
            List<Long> slot = previewUserIds.get(m.getProjectId());
            if (slot == null || slot.size() >= MAX_MEMBER_AVATAR_PREVIEW) {
                continue;
            }
            slot.add(m.getUserId());
        }
        Set<Long> allUserIds = new HashSet<>();
        for (List<Long> u : previewUserIds.values()) {
            allUserIds.addAll(u);
        }
        Map<Long, User> userMap = new HashMap<>();
        if (!allUserIds.isEmpty()) {
            List<User> users = userMapper.selectList(
                    Wrappers.<User>lambdaQuery().in(User::getId, allUserIds));
            for (User u : users) {
                userMap.put(u.getId(), u);
            }
        }
        for (Map.Entry<Long, List<Long>> e : previewUserIds.entrySet()) {
            List<String> urls = new ArrayList<>();
            for (Long uid : e.getValue()) {
                User u = userMap.get(uid);
                if (u != null && u.getAvatarUrl() != null && !u.getAvatarUrl().isBlank()) {
                    urls.add(u.getAvatarUrl());
                }
            }
            out.put(e.getKey(), List.copyOf(urls));
        }
        return out;
    }

    private static Map<Long, Long> mapsToProjectLong(List<Map<String, Object>> rows, String projectKey) {
        Map<Long, Long> out = new HashMap<>();
        if (rows == null) {
            return out;
        }
        for (Map<String, Object> row : rows) {
            Long pid = extractProjectId(row, projectKey);
            if (pid == null) {
                continue;
            }
            out.put(pid, countFromRow(row));
        }
        return out;
    }

    private static long countFromRow(Map<String, Object> row) {
        for (Map.Entry<String, Object> e : row.entrySet()) {
            if (e.getKey() != null && e.getKey().equalsIgnoreCase("c")) {
                return toLong(e.getValue());
            }
        }
        return 0L;
    }

    private static Long extractProjectId(Map<String, Object> row, String logicalKey) {
        Object v = row.get(logicalKey);
        if (v == null) {
            for (String k : row.keySet()) {
                if (k != null && k.equalsIgnoreCase(logicalKey)) {
                    v = row.get(k);
                    break;
                }
            }
        }
        return toLongOrNull(v);
    }

    private static Long toLongOrNull(Object v) {
        if (v == null) {
            return null;
        }
        if (v instanceof Number n) {
            return n.longValue();
        }
        try {
            return Long.parseLong(v.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static long toLong(Object v) {
        Long x = toLongOrNull(v);
        return x != null ? x : 0L;
    }
}
