package com.aiplatform.backend.controller;

import com.aiplatform.backend.dto.ProjectAgentResponse;
import com.aiplatform.backend.dto.UpdateProjectAgentRequest;
import com.aiplatform.backend.entity.Project;
import com.aiplatform.backend.service.ProjectAgentService;
import com.aiplatform.backend.service.ProjectService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * 项目专属智能体管理控制器。
 *
 * <p>提供查询、更新、重建 System Prompt 等管理接口。
 * 智能体的创建由 {@link ProjectController} 在创建项目时自动触发，无需手动调用。</p>
 *
 * <p>API 基路径：{@code /api/projects/{projectId}/agent}</p>
 */
@RestController
@RequestMapping("/api/projects/{projectId}/agent")
public class ProjectAgentController {

    private final ProjectAgentService projectAgentService;
    private final ProjectService projectService;

    public ProjectAgentController(ProjectAgentService projectAgentService,
                                   ProjectService projectService) {
        this.projectAgentService = projectAgentService;
        this.projectService = projectService;
    }

    /**
     * 获取项目专属智能体配置。
     *
     * @param projectId 项目 ID
     * @return 智能体响应 DTO
     */
    @GetMapping
    public ProjectAgentResponse get(@PathVariable Long projectId) {
        // 校验项目存在
        projectService.getByIdOrThrow(projectId);
        return ProjectAgentResponse.from(projectAgentService.getByProjectIdOrThrow(projectId));
    }

    /**
     * 更新项目专属智能体配置。
     *
     * <p>仅更新请求中明确传入的字段（非 null 字段）。
     * 可用于开启部署能力、修改 System Prompt、切换首选模型等。</p>
     *
     * @param projectId 项目 ID
     * @param request   更新请求
     * @return 更新后的智能体响应 DTO
     */
    @PutMapping
    public ProjectAgentResponse update(
            @PathVariable Long projectId,
            @RequestBody UpdateProjectAgentRequest request) {
        projectService.getByIdOrThrow(projectId);
        return ProjectAgentResponse.from(projectAgentService.update(projectId, request));
    }

    /**
     * 手动重建项目智能体的默认 System Prompt。
     *
     * <p>当项目名称、描述等基础信息发生变更后，可调用此接口将
     * System Prompt 重置为基于最新项目信息生成的默认内容。
     * 注意：此操作会覆盖管理员已手动修改的 System Prompt。</p>
     *
     * @param projectId 项目 ID
     * @return 重建后的智能体响应 DTO
     */
    @PostMapping("/rebuild-prompt")
    @ResponseStatus(HttpStatus.OK)
    public ProjectAgentResponse rebuildPrompt(@PathVariable Long projectId) {
        Project project = projectService.getByIdOrThrow(projectId);
        return ProjectAgentResponse.from(projectAgentService.rebuildSystemPrompt(project));
    }
}
