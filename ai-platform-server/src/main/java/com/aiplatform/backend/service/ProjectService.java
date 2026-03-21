package com.aiplatform.backend.service;

import com.aiplatform.backend.common.dto.PageResponse;
import com.aiplatform.backend.dto.CreateProjectRequest;
import com.aiplatform.backend.dto.ProjectResponse;
import com.aiplatform.backend.entity.Project;
import com.aiplatform.backend.mapper.ProjectMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 项目业务服务，提供项目的创建、查询和分页检索等核心操作。
 */
@Service
public class ProjectService {

    /** 按项目类型分配的默认月度 Token 池（单位：Token 数） */
    private static final Map<String, Long> DEFAULT_QUOTA_BY_TYPE = Map.of(
            "PRODUCT",  500_000L,
            "PLATFORM", 800_000L,
            "DATA",     300_000L,
            "OTHER",    300_000L
    );

    private final ProjectMapper projectMapper;

    public ProjectService(ProjectMapper projectMapper) {
        this.projectMapper = projectMapper;
    }

    /**
     * 创建新项目，初始化项目 Token 池配额（双池之二）。
     *
     * <p>若请求未指定 {@code monthlyTokenQuota}，按项目类型取默认值：
     * PRODUCT=500K, PLATFORM=800K, DATA/OTHER=300K。</p>
     *
     * @param request 创建项目的请求参数
     * @return 持久化后的项目实体
     */
    public Project create(CreateProjectRequest request) {
        Project project = new Project();
        project.setName(request.name());
        project.setCode(request.code());
        project.setDescription(request.description());
        project.setIcon(request.icon());
        project.setProjectType(request.projectType());
        project.setOwnerUserId(request.ownerUserId());
        project.setStatus("ACTIVE");

        // 项目 Token 池初始化（双池之二）
        long defaultQuota = DEFAULT_QUOTA_BY_TYPE.getOrDefault(request.projectType(), 300_000L);
        project.setMonthlyTokenQuota(
                request.monthlyTokenQuota() != null ? request.monthlyTokenQuota() : defaultQuota);
        project.setUsedTokensThisMonth(0L);
        project.setAlertThresholdPct(
                request.alertThresholdPct() != null ? request.alertThresholdPct() : 80);
        project.setOverQuotaStrategy(
                request.overQuotaStrategy() != null ? request.overQuotaStrategy() : "BLOCK");

        projectMapper.insert(project);
        return project;
    }

    /**
     * 查询所有项目列表（不分页），按 ID 升序排列。
     *
     * @return 项目实体列表
     */
    public List<Project> list() {
        return projectMapper.selectList(Wrappers.<Project>lambdaQuery().orderByAsc(Project::getId));
    }

    /**
     * 分页查询项目列表，按 ID 升序排列。
     *
     * @param page 页码（从 1 开始）
     * @param size 每页记录数
     * @return 分页响应，包含项目 DTO 列表
     */
    public PageResponse<ProjectResponse> listPaged(int page, int size) {
        Page<Project> result = projectMapper.selectPage(
                new Page<>(page, size),
                Wrappers.<Project>lambdaQuery().orderByAsc(Project::getId)
        );
        return PageResponse.from(result, ProjectResponse::from);
    }

    /**
     * 根据 ID 查询项目，不存在时抛出 {@link com.aiplatform.backend.common.exception.ProjectNotFoundException}。
     *
     * @param projectId 项目 ID
     * @return 项目实体
     */
    public Project getByIdOrThrow(Long projectId) {
        Project project = projectMapper.selectById(projectId);
        if (project == null) {
            throw new com.aiplatform.backend.common.exception.ProjectNotFoundException(projectId);
        }
        return project;
    }
}
