package com.aiplatform.backend.controller;

import com.aiplatform.backend.common.dto.PageResponse;
import com.aiplatform.backend.dto.CreateProjectRequest;
import com.aiplatform.backend.dto.ProjectCardResponse;
import com.aiplatform.backend.dto.ProjectOverviewResponse;
import com.aiplatform.backend.dto.ProjectResponse;
import com.aiplatform.backend.dto.UpdateProjectRequest;
import com.aiplatform.backend.service.ProjectDashboardService;
import com.aiplatform.backend.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * 项目管理控制器。
 *
 * <p>API 基路径：{@code /api/projects}</p>
 */
@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;
    private final ProjectDashboardService projectDashboardService;

    public ProjectController(ProjectService projectService,
                             ProjectDashboardService projectDashboardService) {
        this.projectService = projectService;
        this.projectDashboardService = projectDashboardService;
    }

    /** 创建新项目。 */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectResponse create(@Valid @RequestBody CreateProjectRequest request) {
        return ProjectResponse.from(projectService.create(request));
    }

    /** 分页查询项目列表（支持关键词、状态、类型筛选）。 */
    @GetMapping
    public PageResponse<ProjectResponse> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String projectType) {
        return projectService.listPaged(page, size, keyword, status, projectType);
    }

    /**
     * 项目工作台卡片页聚合数据（单接口渲染项目网格：成员头像、代码服务、AI 能力、Token 等）。
     *
     * <p>须声明在 {@code /{id}} 之前，避免路径 {@code dashboard} 被误解析为 id。</p>
     */
    @GetMapping("/dashboard")
    public PageResponse<ProjectCardResponse> dashboard(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "false") boolean includeArchived,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String projectType) {
        return projectDashboardService.listDashboard(
                page, size, includeArchived, keyword, status, projectType);
    }

    /** 根据 ID 查询单个项目详情。 */
    @GetMapping("/{id}")
    public ProjectResponse getById(@PathVariable Long id) {
        return ProjectResponse.from(projectService.getByIdOrThrow(id));
    }

    /**
     * 更新项目信息（仅更新非 null 字段）。
     *
     * @param id      项目 ID
     * @param request 更新请求
     * @return 更新后的项目响应
     */
    @PutMapping("/{id}")
    public ProjectResponse update(@PathVariable Long id, @Valid @RequestBody UpdateProjectRequest request) {
        return ProjectResponse.from(projectService.update(id, request));
    }

    /**
     * 归档项目（status → ARCHIVED）。
     *
     * @param id 项目 ID
     * @return 归档后的项目响应
     */
    @PostMapping("/{id}/archive")
    public ProjectResponse archive(@PathVariable Long id) {
        return ProjectResponse.from(projectService.archive(id));
    }

    /**
     * 获取项目概览聚合数据（成员数、服务数、Token 用量）。
     *
     * @param id 项目 ID
     * @return 项目概览响应
     */
    @GetMapping("/{id}/overview")
    public ProjectOverviewResponse overview(@PathVariable Long id) {
        return projectService.overview(id);
    }
}
