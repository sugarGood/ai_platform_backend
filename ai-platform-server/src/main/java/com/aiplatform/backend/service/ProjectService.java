package com.aiplatform.backend.service;

import com.aiplatform.backend.common.dto.PageResponse;
import com.aiplatform.backend.dto.CreateProjectRequest;
import com.aiplatform.backend.dto.ProjectOverviewResponse;
import com.aiplatform.backend.dto.ProjectResponse;
import com.aiplatform.backend.dto.UpdateProjectRequest;
import com.aiplatform.backend.entity.Project;
import com.aiplatform.backend.mapper.ProjectMapper;
import com.aiplatform.backend.mapper.ProjectMemberMapper;
import com.aiplatform.backend.mapper.ServiceEntityMapper;
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
    private final ProjectAgentService projectAgentService;
    private final ProjectMemberMapper projectMemberMapper;
    private final ServiceEntityMapper serviceEntityMapper;

    public ProjectService(ProjectMapper projectMapper,
                          ProjectAgentService projectAgentService,
                          ProjectMemberMapper projectMemberMapper,
                          ServiceEntityMapper serviceEntityMapper) {
        this.projectMapper = projectMapper;
        this.projectAgentService = projectAgentService;
        this.projectMemberMapper = projectMemberMapper;
        this.serviceEntityMapper = serviceEntityMapper;
    }

    /**
     * 创建新项目，初始化项目 Token 池配额（双池之二），并自动创建专属智能体。
     *
     * <p>若请求未指定 {@code monthlyTokenQuota}，按项目类型取默认值：
     * PRODUCT=500K, PLATFORM=800K, DATA/OTHER=300K。</p>
     *
     * <p>项目插入成功后，自动调用 {@link ProjectAgentService#initForProject}
     * 为该项目创建一个默认配置的专属智能体。</p>
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

        // 自动为新项目创建专属智能体（createdBy 取项目负责人 ID）
        projectAgentService.initForProject(project, request.ownerUserId());

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

    /**
     * 更新项目信息（仅更新非 null 字段）。
     *
     * @param projectId 项目 ID
     * @param request   更新请求
     * @return 更新后的项目实体
     */
    public Project update(Long projectId, UpdateProjectRequest request) {
        Project project = getByIdOrThrow(projectId);
        if (request.name() != null)              project.setName(request.name());
        if (request.description() != null)       project.setDescription(request.description());
        if (request.icon() != null)              project.setIcon(request.icon());
        if (request.ownerUserId() != null)       project.setOwnerUserId(request.ownerUserId());
        if (request.monthlyTokenQuota() != null) project.setMonthlyTokenQuota(request.monthlyTokenQuota());
        if (request.alertThresholdPct() != null) project.setAlertThresholdPct(request.alertThresholdPct());
        if (request.overQuotaStrategy() != null) project.setOverQuotaStrategy(request.overQuotaStrategy());
        projectMapper.updateById(project);
        return project;
    }

    /**
     * 归档项目（status → ARCHIVED）。
     *
     * @param projectId 项目 ID
     * @return 更新后的项目实体
     */
    public Project archive(Long projectId) {
        Project project = getByIdOrThrow(projectId);
        project.setStatus("ARCHIVED");
        projectMapper.updateById(project);
        return project;
    }

    /**
     * 获取项目概览聚合数据（成员数、服务数、Token 用量）。
     *
     * @param projectId 项目 ID
     * @return 项目概览响应
     */
    public ProjectOverviewResponse overview(Long projectId) {
        Project project = getByIdOrThrow(projectId);
        long memberCount = projectMemberMapper.selectCount(
                Wrappers.lambdaQuery(com.aiplatform.backend.entity.ProjectMember.class)
                        .eq(com.aiplatform.backend.entity.ProjectMember::getProjectId, projectId)
        );
        long serviceCount = serviceEntityMapper.selectCount(
                Wrappers.lambdaQuery(com.aiplatform.backend.entity.ServiceEntity.class)
                        .eq(com.aiplatform.backend.entity.ServiceEntity::getProjectId, projectId)
        );
        Long quota = project.getMonthlyTokenQuota();
        Long used  = project.getUsedTokensThisMonth() != null ? project.getUsedTokensThisMonth() : 0L;
        Double usedPct = (quota != null && quota > 0) ? (used * 100.0 / quota) : null;
        return new ProjectOverviewResponse(
                project.getId(),
                project.getName(),
                project.getStatus(),
                memberCount,
                serviceCount,
                quota,
                used,
                usedPct,
                project.getOverQuotaStrategy()
        );
    }
}
