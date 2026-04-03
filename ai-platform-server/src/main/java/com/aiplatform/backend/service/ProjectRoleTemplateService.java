package com.aiplatform.backend.service;

import com.aiplatform.backend.common.exception.BusinessException;
import com.aiplatform.backend.dto.CreateProjectRoleTemplateRequest;
import com.aiplatform.backend.dto.ProjectMemberModuleAccessResponse;
import com.aiplatform.backend.dto.ProjectPermissionMatrixResponse;
import com.aiplatform.backend.dto.ProjectRoleTemplatePermissionsRequest;
import com.aiplatform.backend.dto.ProjectRoleTemplateResponse;
import com.aiplatform.backend.dto.UpdateProjectRoleTemplateRequest;
import com.aiplatform.backend.entity.ProjectRoleTemplate;
import com.aiplatform.backend.entity.ProjectRoleTemplatePermission;
import com.aiplatform.backend.mapper.ProjectRoleTemplateMapper;
import com.aiplatform.backend.mapper.ProjectRoleTemplatePermissionMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 项目角色模板服务，负责模板与权限矩阵编排。
 */
@Service
public class ProjectRoleTemplateService {

    private final ProjectRoleTemplateMapper projectRoleTemplateMapper;
    private final ProjectRoleTemplatePermissionMapper projectRoleTemplatePermissionMapper;
    private final ProjectService projectService;

    public ProjectRoleTemplateService(ProjectRoleTemplateMapper projectRoleTemplateMapper,
                                      ProjectRoleTemplatePermissionMapper projectRoleTemplatePermissionMapper,
                                      ProjectService projectService) {
        this.projectRoleTemplateMapper = projectRoleTemplateMapper;
        this.projectRoleTemplatePermissionMapper = projectRoleTemplatePermissionMapper;
        this.projectService = projectService;
    }

    /**
     * 查询项目角色模板列表。
     */
    public List<ProjectRoleTemplateResponse> list(Long projectId) {
        projectService.getByIdOrThrow(projectId);
        return projectRoleTemplateMapper.selectList(
                        Wrappers.<ProjectRoleTemplate>lambdaQuery()
                                .eq(ProjectRoleTemplate::getProjectId, projectId)
                                .orderByAsc(ProjectRoleTemplate::getId))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * 新建项目角色模板。
     */
    @Transactional(rollbackFor = Throwable.class)
    public ProjectRoleTemplateResponse create(Long projectId, CreateProjectRoleTemplateRequest request) {
        projectService.getByIdOrThrow(projectId);
        ProjectRoleTemplate template = new ProjectRoleTemplate();
        template.setProjectId(projectId);
        template.setRoleCode(request.roleCode());
        template.setTemplateName(request.templateName());
        template.setDescription(request.description());
        template.setStatus("ACTIVE");
        projectRoleTemplateMapper.insert(template);
        return toResponse(template);
    }

    /**
     * 编辑项目角色模板。
     */
    @Transactional(rollbackFor = Throwable.class)
    public ProjectRoleTemplateResponse update(Long projectId, Long roleTemplateId, UpdateProjectRoleTemplateRequest request) {
        ProjectRoleTemplate template = requireTemplate(projectId, roleTemplateId);
        if (request.templateName() != null) {
            template.setTemplateName(request.templateName());
        }
        if (request.description() != null) {
            template.setDescription(request.description());
        }
        if (request.status() != null) {
            template.setStatus(request.status());
        }
        projectRoleTemplateMapper.updateById(template);
        return toResponse(template);
    }

    /**
     * 保存项目角色模板的模块权限矩阵。
     */
    @Transactional(rollbackFor = Throwable.class)
    public ProjectPermissionMatrixResponse savePermissions(Long projectId,
                                                           Long roleTemplateId,
                                                           ProjectRoleTemplatePermissionsRequest request) {
        requireTemplate(projectId, roleTemplateId);
        projectRoleTemplatePermissionMapper.delete(
                Wrappers.<ProjectRoleTemplatePermission>lambdaQuery()
                        .eq(ProjectRoleTemplatePermission::getProjectRoleTemplateId, roleTemplateId));

        if (request.modulePermissions() != null) {
            for (var permission : request.modulePermissions()) {
                ProjectRoleTemplatePermission row = new ProjectRoleTemplatePermission();
                row.setProjectRoleTemplateId(roleTemplateId);
                row.setModuleKey(permission.moduleKey());
                row.setAccessLevel(permission.accessLevel());
                projectRoleTemplatePermissionMapper.insert(row);
            }
        }
        return getPermissionMatrix(projectId, roleTemplateId);
    }

    /**
     * 获取指定角色模板的权限矩阵。
     */
    public ProjectPermissionMatrixResponse getPermissionMatrix(Long projectId, Long roleTemplateId) {
        requireTemplate(projectId, roleTemplateId);
        List<ProjectMemberModuleAccessResponse> modulePermissions = projectRoleTemplatePermissionMapper.selectList(
                        Wrappers.<ProjectRoleTemplatePermission>lambdaQuery()
                                .eq(ProjectRoleTemplatePermission::getProjectRoleTemplateId, roleTemplateId)
                                .orderByAsc(ProjectRoleTemplatePermission::getId))
                .stream()
                .map(item -> new ProjectMemberModuleAccessResponse(item.getModuleKey(), item.getAccessLevel(), false))
                .toList();
        return new ProjectPermissionMatrixResponse(projectId, roleTemplateId, modulePermissions);
    }

    private ProjectRoleTemplate requireTemplate(Long projectId, Long roleTemplateId) {
        projectService.getByIdOrThrow(projectId);
        ProjectRoleTemplate template = projectRoleTemplateMapper.selectOne(
                Wrappers.<ProjectRoleTemplate>lambdaQuery()
                        .eq(ProjectRoleTemplate::getProjectId, projectId)
                        .eq(ProjectRoleTemplate::getId, roleTemplateId)
                        .last("LIMIT 1"));
        if (template == null) {
            throw new BusinessException(404, "PROJECT_ROLE_TEMPLATE_NOT_FOUND", "项目角色模板不存在");
        }
        return template;
    }

    private ProjectRoleTemplateResponse toResponse(ProjectRoleTemplate template) {
        return new ProjectRoleTemplateResponse(
                template.getId(),
                template.getProjectId(),
                template.getRoleCode(),
                template.getTemplateName(),
                template.getDescription(),
                template.getStatus()
        );
    }
}
