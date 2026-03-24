package com.aiplatform.backend.controller;

import com.aiplatform.backend.common.dto.PageResponse;
import com.aiplatform.backend.dto.CreateProjectRequest;
import com.aiplatform.backend.dto.ProjectOverviewResponse;
import com.aiplatform.backend.dto.ProjectResponse;
import com.aiplatform.backend.dto.UpdateProjectRequest;
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

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    /** 创建新项目。 */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectResponse create(@Valid @RequestBody CreateProjectRequest request) {
        return ProjectResponse.from(projectService.create(request));
    }

    /** 分页查询项目列表。 */
    @GetMapping
    public PageResponse<ProjectResponse> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return projectService.listPaged(page, size);
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
    public ProjectResponse update(@PathVariable Long id, @RequestBody UpdateProjectRequest request) {
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
