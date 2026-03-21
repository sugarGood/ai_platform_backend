package com.aiplatform.backend.controller;

import com.aiplatform.backend.dto.CreateProjectMemberRequest;
import com.aiplatform.backend.dto.ProjectMemberResponse;
import com.aiplatform.backend.service.ProjectMemberService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 项目成员管理控制器，提供项目成员的添加、移除和列表查询接口。
 *
 * <p>API 基路径：{@code /api/projects/{projectId}/members}</p>
 */
@RestController
@RequestMapping("/api/projects/{projectId}/members")
public class ProjectMemberController {

    private final ProjectMemberService projectMemberService;

    public ProjectMemberController(ProjectMemberService projectMemberService) {
        this.projectMemberService = projectMemberService;
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
        return ProjectMemberResponse.from(projectMemberService.create(projectId, request));
    }

    /**
     * 查询指定项目下的所有成员列表。
     *
     * @param projectId 项目 ID（路径参数）
     * @return 项目成员响应 DTO 列表
     */
    @GetMapping
    public List<ProjectMemberResponse> list(@PathVariable Long projectId) {
        return projectMemberService.listByProjectId(projectId).stream()
                .map(ProjectMemberResponse::from)
                .toList();
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
