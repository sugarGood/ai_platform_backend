package com.aiplatform.backend.service;

import com.aiplatform.backend.common.dto.PageResponse;
import com.aiplatform.backend.common.exception.ProjectNotFoundException;
import com.aiplatform.backend.dto.CreateProjectRequest;
import com.aiplatform.backend.dto.ProjectListQuery;
import com.aiplatform.backend.dto.ProjectOverviewResponse;
import com.aiplatform.backend.dto.ProjectResponse;
import com.aiplatform.backend.dto.UpdateProjectRequest;
import com.aiplatform.backend.entity.Project;
import com.aiplatform.backend.entity.ProjectMember;
import com.aiplatform.backend.entity.ServiceEntity;
import com.aiplatform.backend.mapper.ProjectMapper;
import com.aiplatform.backend.mapper.ProjectMemberMapper;
import com.aiplatform.backend.mapper.ServiceEntityMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 项目业务服务，提供项目创建、查询、更新和概览聚合能力。
 */
@Service
public class ProjectService {

    private static final String STATUS_ACTIVE = "ACTIVE";
    private static final String STATUS_ARCHIVED = "ARCHIVED";
    private static final String DEFAULT_OVER_QUOTA_STRATEGY = "BLOCK";
    private static final String DEFAULT_QUOTA_RESET_CYCLE = "MONTHLY";
    private static final long DEFAULT_TOKEN_QUOTA = 300_000L;

    private static final Map<String, Long> DEFAULT_QUOTA_BY_TYPE = Map.of(
            "PRODUCT", 500_000L,
            "PLATFORM", 800_000L,
            "DATA", DEFAULT_TOKEN_QUOTA,
            "OTHER", DEFAULT_TOKEN_QUOTA
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
     * 创建项目，并初始化项目级 Token 配额和默认专属智能体。
     */
    public Project create(CreateProjectRequest request) {
        Project project = new Project();
        project.setName(request.name());
        project.setCode(request.code());
        project.setDescription(request.description());
        project.setIcon(request.icon());
        project.setProjectType(request.projectType());
        project.setOwnerUserId(request.ownerUserId());
        project.setStatus(STATUS_ACTIVE);
        initializeTokenQuota(project, request);

        projectMapper.insert(project);
        projectAgentService.initForProject(project, request.ownerUserId());
        return project;
    }

    /**
     * 查询全部项目列表，不分页。
     */
    public List<Project> list() {
        return projectMapper.selectList(Wrappers.<Project>lambdaQuery().orderByAsc(Project::getId));
    }

    /**
     * 分页查询项目列表。
     */
    public PageResponse<ProjectResponse> listPaged(ProjectListQuery query) {
        var wrapper = Wrappers.<Project>lambdaQuery().orderByAsc(Project::getId);
        ProjectQueryFilters.applyForList(wrapper, query.keyword(), query.status(), query.projectType());
        Page<Project> result = projectMapper.selectPage(new Page<>(query.page(), query.size()), wrapper);
        return PageResponse.from(result, ProjectResponse::from);
    }

    /**
     * 根据 ID 获取项目，不存在时抛出异常。
     */
    public Project getByIdOrThrow(Long projectId) {
        Project project = projectMapper.selectById(projectId);
        if (project == null) {
            throw new ProjectNotFoundException(projectId);
        }
        return project;
    }

    /**
     * 更新项目信息，仅写入非 null 字段。
     */
    public Project update(Long projectId, UpdateProjectRequest request) {
        Project project = getByIdOrThrow(projectId);
        applyUpdates(project, request);
        projectMapper.updateById(project);
        return project;
    }

    /**
     * 归档项目。
     */
    public Project archive(Long projectId) {
        Project project = getByIdOrThrow(projectId);
        project.setStatus(STATUS_ARCHIVED);
        projectMapper.updateById(project);
        return project;
    }

    /**
     * 获取项目概览聚合信息。
     */
    public ProjectOverviewResponse overview(Long projectId) {
        Project project = getByIdOrThrow(projectId);
        long memberCount = projectMemberMapper.selectCount(
                Wrappers.lambdaQuery(ProjectMember.class)
                        .eq(ProjectMember::getProjectId, projectId)
        );
        long serviceCount = serviceEntityMapper.selectCount(
                Wrappers.lambdaQuery(ServiceEntity.class)
                        .eq(ServiceEntity::getProjectId, projectId)
        );
        Long quota = project.getMonthlyTokenQuota();
        long used = project.getUsedTokensThisMonth() != null ? project.getUsedTokensThisMonth() : 0L;
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

    private void initializeTokenQuota(Project project, CreateProjectRequest request) {
        long defaultQuota = DEFAULT_QUOTA_BY_TYPE.getOrDefault(request.projectType(), DEFAULT_TOKEN_QUOTA);
        project.setMonthlyTokenQuota(
                request.monthlyTokenQuota() != null ? request.monthlyTokenQuota() : defaultQuota);
        project.setUsedTokensThisMonth(0L);
        project.setAlertThresholdPct(
                request.alertThresholdPct() != null ? request.alertThresholdPct() : 80);
        project.setOverQuotaStrategy(
                request.overQuotaStrategy() != null ? request.overQuotaStrategy() : DEFAULT_OVER_QUOTA_STRATEGY);
        project.setQuotaResetCycle(DEFAULT_QUOTA_RESET_CYCLE);
        project.setSingleRequestTokenCap(null);
    }

    private void applyUpdates(Project project, UpdateProjectRequest request) {
        if (request.name() != null) {
            project.setName(request.name());
        }
        if (request.description() != null) {
            project.setDescription(request.description());
        }
        if (request.icon() != null) {
            project.setIcon(request.icon());
        }
        if (request.projectType() != null) {
            project.setProjectType(request.projectType());
        }
        if (request.ownerUserId() != null) {
            project.setOwnerUserId(request.ownerUserId());
        }
        if (request.monthlyTokenQuota() != null) {
            project.setMonthlyTokenQuota(request.monthlyTokenQuota());
        }
        if (request.alertThresholdPct() != null) {
            project.setAlertThresholdPct(request.alertThresholdPct());
        }
        if (request.overQuotaStrategy() != null) {
            project.setOverQuotaStrategy(request.overQuotaStrategy());
        }
        if (request.quotaResetCycle() != null) {
            project.setQuotaResetCycle(request.quotaResetCycle());
        }
        if (request.singleRequestTokenCap() != null) {
            project.setSingleRequestTokenCap(
                    request.singleRequestTokenCap() == 0 ? null : request.singleRequestTokenCap());
        }
    }
}
