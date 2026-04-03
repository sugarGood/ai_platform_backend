package com.aiplatform.backend.controller;

import com.aiplatform.backend.dto.CreateProjectRoleTemplateRequest;
import com.aiplatform.backend.dto.ProjectPermissionMatrixResponse;
import com.aiplatform.backend.dto.ProjectRoleTemplatePermissionsRequest;
import com.aiplatform.backend.dto.ProjectRoleTemplateResponse;
import com.aiplatform.backend.dto.UpdateProjectRoleTemplateRequest;
import com.aiplatform.backend.service.ProjectRoleTemplateService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
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
 * 项目角色模板控制器。
 */
@RestController
@RequestMapping("/api/projects/{projectId}/role-templates")
public class ProjectRoleTemplateController {

    private final ProjectRoleTemplateService projectRoleTemplateService;

    public ProjectRoleTemplateController(ProjectRoleTemplateService projectRoleTemplateService) {
        this.projectRoleTemplateService = projectRoleTemplateService;
    }

    /**
     * 查询项目角色模板列表。
     */
    @GetMapping
    public List<ProjectRoleTemplateResponse> list(@PathVariable Long projectId) {
        return projectRoleTemplateService.list(projectId);
    }

    /**
     * 新建项目角色模板。
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectRoleTemplateResponse create(@PathVariable Long projectId,
                                              @Valid @RequestBody CreateProjectRoleTemplateRequest request) {
        return projectRoleTemplateService.create(projectId, request);
    }

    /**
     * 编辑项目角色模板。
     */
    @PutMapping("/{roleTemplateId}")
    public ProjectRoleTemplateResponse update(@PathVariable Long projectId,
                                              @PathVariable Long roleTemplateId,
                                              @RequestBody UpdateProjectRoleTemplateRequest request) {
        return projectRoleTemplateService.update(projectId, roleTemplateId, request);
    }

    /**
     * 保存模板权限矩阵。
     */
    @PostMapping("/{roleTemplateId}/permission-matrix")
    public ProjectPermissionMatrixResponse savePermissionMatrix(@PathVariable Long projectId,
                                                                @PathVariable Long roleTemplateId,
                                                                @Valid @RequestBody ProjectRoleTemplatePermissionsRequest request) {
        return projectRoleTemplateService.savePermissions(projectId, roleTemplateId, request);
    }

    /**
     * 查询模板权限矩阵。
     */
    @GetMapping("/{roleTemplateId}/permission-matrix")
    public ProjectPermissionMatrixResponse permissionMatrix(@PathVariable Long projectId,
                                                            @PathVariable Long roleTemplateId) {
        return projectRoleTemplateService.getPermissionMatrix(projectId, roleTemplateId);
    }
}
