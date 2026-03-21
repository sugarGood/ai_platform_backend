package com.aiplatform.backend.controller;

import com.aiplatform.backend.common.dto.PageResponse;
import com.aiplatform.backend.dto.CreateProjectRequest;
import com.aiplatform.backend.dto.ProjectResponse;
import com.aiplatform.backend.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * 项目管理控制器，提供项目的创建、分页查询和按 ID 查询接口。
 *
 * <p>API 基路径：{@code /api/projects}</p>
 */
@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    /** 项目业务服务 */
    private final ProjectService projectService;

    /**
     * 构造函数，注入项目业务服务。
     *
     * @param projectService 项目业务服务
     */
    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    /**
     * 创建新项目。
     *
     * @param request 创建项目的请求参数（经过参数校验）
     * @return 新创建的项目响应 DTO
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectResponse create(@Valid @RequestBody CreateProjectRequest request) {
        return ProjectResponse.from(projectService.create(request));
    }

    /**
     * 分页查询项目列表。
     *
     * @param page 页码，默认 1
     * @param size 每页记录数，默认 20
     * @return 分页响应，包含项目 DTO 列表
     */
    @GetMapping
    public PageResponse<ProjectResponse> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return projectService.listPaged(page, size);
    }

    /**
     * 根据 ID 查询单个项目详情。
     *
     * @param id 项目 ID
     * @return 项目响应 DTO
     */
    @GetMapping("/{id}")
    public ProjectResponse getById(@PathVariable Long id) {
        return ProjectResponse.from(projectService.getByIdOrThrow(id));
    }
}
