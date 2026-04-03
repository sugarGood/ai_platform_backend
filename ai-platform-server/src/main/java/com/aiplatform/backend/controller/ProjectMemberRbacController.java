package com.aiplatform.backend.controller;

import com.aiplatform.backend.dto.ProjectMemberPermissionOverridesRequest;
import com.aiplatform.backend.dto.ProjectMemberPermissionOverridesResponse;
import com.aiplatform.backend.dto.ProjectMemberResourceGrantsRequest;
import com.aiplatform.backend.service.ProjectMemberRbacService;
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

/**
 * 项目成员 RBAC 控制器。
 */
@RestController
@RequestMapping("/api/projects/{projectId}/members/{memberId}")
public class ProjectMemberRbacController {

    private final ProjectMemberRbacService projectMemberRbacService;

    public ProjectMemberRbacController(ProjectMemberRbacService projectMemberRbacService) {
        this.projectMemberRbacService = projectMemberRbacService;
    }

    /**
     * 保存成员模块权限覆写。
     */
    @PostMapping("/permission-overrides")
    public ProjectMemberPermissionOverridesResponse savePermissionOverrides(
            @PathVariable Long projectId,
            @PathVariable Long memberId,
            @Valid @RequestBody ProjectMemberPermissionOverridesRequest request) {
        return projectMemberRbacService.savePermissionOverrides(projectId, memberId, request);
    }

    /**
     * 保存成员资源授权范围。
     */
    @PostMapping("/resource-grants")
    public ProjectMemberPermissionOverridesResponse saveResourceGrants(
            @PathVariable Long projectId,
            @PathVariable Long memberId,
            @RequestBody ProjectMemberResourceGrantsRequest request) {
        return projectMemberRbacService.saveResourceGrants(projectId, memberId, request);
    }

    /**
     * 查询成员能力收敛结果。
     */
    @GetMapping("/abilities")
    public ProjectMemberPermissionOverridesResponse abilities(@PathVariable Long projectId,
                                                              @PathVariable Long memberId) {
        return projectMemberRbacService.getPermissionOverrides(projectId, memberId);
    }

    /**
     * 清空成员模块覆写。
     */
    @DeleteMapping("/permission-overrides")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clearPermissionOverrides(@PathVariable Long projectId,
                                         @PathVariable Long memberId) {
        projectMemberRbacService.clearPermissionOverrides(projectId, memberId);
    }

    /**
     * 清空成员资源授权。
     */
    @DeleteMapping("/resource-grants")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clearResourceGrants(@PathVariable Long projectId,
                                    @PathVariable Long memberId) {
        projectMemberRbacService.clearResourceGrants(projectId, memberId);
    }
}
