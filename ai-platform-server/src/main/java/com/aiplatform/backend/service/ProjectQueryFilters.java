package com.aiplatform.backend.service;

import com.aiplatform.backend.common.exception.BusinessException;
import com.aiplatform.backend.entity.Project;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import java.util.Locale;
import java.util.Set;

/**
 * 项目列表/工作台共用的关键词、状态、类型筛选（与前端搜索条一致）。
 */
public final class ProjectQueryFilters {

    private static final Set<String> STATUSES = Set.of("ACTIVE", "ARCHIVED");
    private static final Set<String> TYPES = Set.of("PRODUCT", "PLATFORM", "DATA", "OTHER");

    private ProjectQueryFilters() {
    }

    /**
     * 工作台卡片：未指定状态时，默认仅 ACTIVE；{@code includeArchived=true} 且状态为「全部」时不按状态过滤。
     */
    public static void applyForDashboard(
            LambdaQueryWrapper<Project> wrapper,
            String keyword,
            String status,
            String projectType,
            boolean includeArchived) {
        applyStatusDashboard(wrapper, status, includeArchived);
        applyProjectType(wrapper, projectType);
        applyKeyword(wrapper, keyword);
    }

    /**
     * 分页列表：未指定状态时不改变原有语义（不过滤状态）；指定 ACTIVE/ARCHIVED 时过滤。
     */
    public static void applyForList(
            LambdaQueryWrapper<Project> wrapper,
            String keyword,
            String status,
            String projectType) {
        applyStatusList(wrapper, status);
        applyProjectType(wrapper, projectType);
        applyKeyword(wrapper, keyword);
    }

    private static void applyStatusDashboard(
            LambdaQueryWrapper<Project> w, String status, boolean includeArchived) {
        String s = normalizeToken(status);
        if (s == null || isAllToken(s)) {
            if (!includeArchived) {
                w.eq(Project::getStatus, "ACTIVE");
            }
            return;
        }
        String u = s.toUpperCase(Locale.ROOT);
        if (!STATUSES.contains(u)) {
            throw new BusinessException(400, "INVALID_PROJECT_STATUS",
                    "status 无效，请使用 ALL、ACTIVE 或 ARCHIVED");
        }
        w.eq(Project::getStatus, u);
    }

    private static void applyStatusList(LambdaQueryWrapper<Project> w, String status) {
        String s = normalizeToken(status);
        if (s == null || isAllToken(s)) {
            return;
        }
        String u = s.toUpperCase(Locale.ROOT);
        if (!STATUSES.contains(u)) {
            throw new BusinessException(400, "INVALID_PROJECT_STATUS",
                    "status 无效，请使用 ALL、ACTIVE 或 ARCHIVED");
        }
        w.eq(Project::getStatus, u);
    }

    private static void applyProjectType(LambdaQueryWrapper<Project> w, String projectType) {
        String t = normalizeToken(projectType);
        if (t == null || isAllToken(t)) {
            return;
        }
        String u = t.toUpperCase(Locale.ROOT);
        if (!TYPES.contains(u)) {
            throw new BusinessException(400, "INVALID_PROJECT_TYPE",
                    "projectType 无效，请使用 PRODUCT、PLATFORM、DATA、OTHER");
        }
        w.eq(Project::getProjectType, u);
    }

    /**
     * 关键词匹配：名称、编码、描述模糊；ID 按字符串形式模糊匹配（支持局部数字）。
     */
    private static void applyKeyword(LambdaQueryWrapper<Project> w, String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return;
        }
        String raw = keyword.trim();
        String like = "%" + escapeLikePattern(raw) + "%";
        w.and(q -> q.like(Project::getName, like)
                .or().like(Project::getCode, like)
                .or().like(Project::getDescription, like)
                .or().apply("CAST(id AS CHAR) LIKE {0}", like));
    }

    private static String escapeLikePattern(String s) {
        return s.replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }

    private static String normalizeToken(String raw) {
        if (raw == null) {
            return null;
        }
        String t = raw.trim();
        return t.isEmpty() ? null : t;
    }

    private static boolean isAllToken(String s) {
        return s.equalsIgnoreCase("all")
                || "*".equals(s)
                || "全部".equals(s)
                || "全部状态".equals(s)
                || "全部类型".equals(s);
    }
}
