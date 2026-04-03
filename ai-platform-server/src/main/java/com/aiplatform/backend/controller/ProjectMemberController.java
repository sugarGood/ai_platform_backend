package com.aiplatform.backend.controller;

import com.aiplatform.backend.dto.CreateProjectMemberRequest;
import com.aiplatform.backend.dto.ProjectMemberResponse;
import com.aiplatform.backend.dto.UpdateProjectMemberRoleRequest;
import com.aiplatform.backend.service.ProjectMemberRbacService;
import com.aiplatform.backend.service.ProjectMemberService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 项目成员管理控制器，提供项目成员的添加、角色更新、移除和列表查询接口。
 *
 * <p>API 基路径：{@code /api/projects/{projectId}/members}</p>
 */
@RestController
@RequestMapping("/api/projects/{projectId}/members")
public class ProjectMemberController {

    private final ProjectMemberService projectMemberService;
    private final ProjectMemberRbacService projectMemberRbacService;

    public ProjectMemberController(ProjectMemberService projectMemberService,
                                   ProjectMemberRbacService projectMemberRbacService) {
        this.projectMemberService = projectMemberService;
        this.projectMemberRbacService = projectMemberRbacService;
    }

    /**
     * 向指定项目中添加成员。
     *
     * @param projectId 项目 ID（路径参数）
     * @param request   添加成员的请求参数
     * @return 新添加的成员响应 DTO
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectMemberResponse create(@PathVariable Long projectId,
                                        @Valid @RequestBody CreateProjectMemberRequest request) {
        return projectMemberService.toResponse(projectMemberService.create(projectId, request));
    }

    /**
     * 查询指定项目下的所有成员列表。
     *
     * @param projectId 项目 ID（路径参数）
     * @return 项目成员响应 DTO 列表
     */
    @GetMapping
    public List<ProjectMemberResponse> list(@PathVariable Long projectId) {
        return projectMemberService.listResponsesByProjectId(projectId);
    }

    /**
     * 更新项目成员角色。
     *
     * @param projectId 项目 ID（路径参数）
     * @param memberId  成员记录 ID（路径参数）
     * @param request   请求体，包含 role 与 resetAbilitiesToRoleDefault 字段
     * @return 更新后的成员响应 DTO
     */
    @PutMapping("/{memberId}")
    public ProjectMemberResponse updateRole(@PathVariable Long projectId,
                                            @PathVariable Long memberId,
                                            @Valid @RequestBody UpdateProjectMemberRoleRequest request) {
        ProjectMemberResponse response = projectMemberService.toResponse(
                projectMemberService.updateRole(projectId, memberId, request.role()));
        if (Boolean.TRUE.equals(request.resetAbilitiesToRoleDefault())) {
            projectMemberRbacService.clearPermissionOverrides(projectId, memberId);
            projectMemberRbacService.clearResourceGrants(projectId, memberId);
        }
        return response;
    }

    /**
     * 从项目中移除成员。
     *
     * @param projectId 项目 ID（路径参数）
     * @param memberId  成员记录 ID（路径参数）
     */
    @DeleteMapping("/{memberId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(@PathVariable Long projectId, @PathVariable Long memberId) {
        projectMemberService.remove(projectId, memberId);
    }
}
