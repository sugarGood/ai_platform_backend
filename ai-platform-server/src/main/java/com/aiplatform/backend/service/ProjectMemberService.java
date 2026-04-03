package com.aiplatform.backend.service;

import com.aiplatform.backend.common.exception.ProjectMemberAlreadyExistsException;
import com.aiplatform.backend.common.exception.ProjectMemberNotFoundException;
import com.aiplatform.backend.common.exception.ProjectNotFoundException;
import com.aiplatform.backend.dto.CreateProjectMemberRequest;
import com.aiplatform.backend.dto.ProjectMemberResponse;
import com.aiplatform.backend.entity.Project;
import com.aiplatform.backend.entity.ProjectMember;
import com.aiplatform.backend.mapper.ProjectMapper;
import com.aiplatform.backend.mapper.ProjectMemberMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 项目成员业务服务，负责成员增删改查和响应组装。
 */
@Service
public class ProjectMemberService {

    private static final String DEFAULT_PROJECT_ROLE = "DEVELOPER";

    private final ProjectMapper projectMapper;
    private final ProjectMemberMapper projectMemberMapper;
    private final PlatformCredentialService platformCredentialService;

    public ProjectMemberService(ProjectMapper projectMapper,
                                ProjectMemberMapper projectMemberMapper,
                                PlatformCredentialService platformCredentialService) {
        this.projectMapper = projectMapper;
        this.projectMemberMapper = projectMemberMapper;
        this.platformCredentialService = platformCredentialService;
    }

    /**
     * 组装包含凭证状态的成员响应。
     */
    public ProjectMemberResponse toResponse(ProjectMember member) {
        return ProjectMemberResponse.from(member, platformCredentialService.getByUserId(member.getUserId()));
    }

    /**
     * 向指定项目添加成员。
     */
    public ProjectMember create(Long projectId, CreateProjectMemberRequest request) {
        ensureProjectExists(projectId);
        if (memberExists(projectId, request.userId())) {
            throw new ProjectMemberAlreadyExistsException(projectId, request.userId());
        }

        ProjectMember member = new ProjectMember();
        member.setProjectId(projectId);
        member.setUserId(request.userId());
        member.setRole(request.role() != null ? request.role() : DEFAULT_PROJECT_ROLE);
        projectMemberMapper.insert(member);
        return member;
    }

    /**
     * 从项目中移除成员。
     */
    public void remove(Long projectId, Long memberId) {
        ensureProjectExists(projectId);
        requireMember(projectId, memberId);
        projectMemberMapper.deleteById(memberId);
    }

    /**
     * 查询项目成员列表。
     */
    public List<ProjectMember> listByProjectId(Long projectId) {
        ensureProjectExists(projectId);
        return projectMemberMapper.selectList(
                Wrappers.<ProjectMember>lambdaQuery()
                        .eq(ProjectMember::getProjectId, projectId)
                        .orderByAsc(ProjectMember::getId)
        );
    }

    /**
     * 查询项目成员响应列表。
     */
    public List<ProjectMemberResponse> listResponsesByProjectId(Long projectId) {
        return listByProjectId(projectId).stream().map(this::toResponse).toList();
    }

    /**
     * 获取单个项目成员。
     */
    public ProjectMember getByProjectAndId(Long projectId, Long memberId) {
        ensureProjectExists(projectId);
        return requireMember(projectId, memberId);
    }

    /**
     * 按项目与用户查询成员记录。
     */
    public ProjectMember getByProjectAndUserId(Long projectId, Long userId) {
        ensureProjectExists(projectId);
        ProjectMember member = projectMemberMapper.selectOne(
                Wrappers.<ProjectMember>lambdaQuery()
                        .eq(ProjectMember::getProjectId, projectId)
                        .eq(ProjectMember::getUserId, userId)
                        .last("LIMIT 1")
        );
        if (member == null) {
            throw new ProjectMemberNotFoundException(projectId, userId, true);
        }
        return member;
    }

    /**
     * 更新项目成员角色。
     */
    public ProjectMember updateRole(Long projectId, Long memberId, String role) {
        ProjectMember member = getByProjectAndId(projectId, memberId);
        member.setRole(role);
        projectMemberMapper.updateById(member);
        return member;
    }

    /**
     * 查询项目实体。
     */
    public Project getProjectById(Long projectId) {
        ensureProjectExists(projectId);
        return projectMapper.selectById(projectId);
    }

    private void ensureProjectExists(Long projectId) {
        if (projectMapper.selectById(projectId) == null) {
            throw new ProjectNotFoundException(projectId);
        }
    }

    private boolean memberExists(Long projectId, Long userId) {
        return projectMemberMapper.selectCount(
                Wrappers.<ProjectMember>lambdaQuery()
                        .eq(ProjectMember::getProjectId, projectId)
                        .eq(ProjectMember::getUserId, userId)
        ) > 0;
    }

    private ProjectMember requireMember(Long projectId, Long memberId) {
        ProjectMember member = projectMemberMapper.selectOne(
                Wrappers.<ProjectMember>lambdaQuery()
                        .eq(ProjectMember::getProjectId, projectId)
                        .eq(ProjectMember::getId, memberId)
                        .last("LIMIT 1")
        );
        if (member == null) {
            throw new ProjectMemberNotFoundException(projectId, memberId);
        }
        return member;
    }
}
