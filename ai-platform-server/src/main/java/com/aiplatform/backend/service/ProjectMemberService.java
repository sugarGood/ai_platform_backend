package com.aiplatform.backend.service;

import com.aiplatform.backend.common.exception.ProjectMemberAlreadyExistsException;
import com.aiplatform.backend.common.exception.ProjectMemberNotFoundException;
import com.aiplatform.backend.dto.CreateProjectMemberRequest;
import com.aiplatform.backend.entity.ProjectMember;
import com.aiplatform.backend.mapper.ProjectMapper;
import com.aiplatform.backend.mapper.ProjectMemberMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 项目成员业务服务，提供成员的添加、列表查询和单个查询等操作。
 */
@Service
public class ProjectMemberService {

    /** 项目数据访问 Mapper */
    private final ProjectMapper projectMapper;
    /** 项目成员数据访问 Mapper */
    private final ProjectMemberMapper projectMemberMapper;

    /**
     * 构造函数，注入所需的 Mapper。
     *
     * @param projectMapper       项目数据访问接口
     * @param projectMemberMapper 项目成员数据访问接口
     */
    public ProjectMemberService(ProjectMapper projectMapper, ProjectMemberMapper projectMemberMapper) {
        this.projectMapper = projectMapper;
        this.projectMemberMapper = projectMemberMapper;
    }

    /**
     * 向指定项目中添加成员。若该用户已是项目成员则抛出异常。
     * 当未指定角色时默认为 MEMBER。
     *
     * @param projectId 项目 ID
     * @param request   添加成员的请求参数
     * @return 持久化后的项目成员实体
     * @throws ProjectMemberAlreadyExistsException 用户已是该项目成员时
     */
    public ProjectMember create(Long projectId, CreateProjectMemberRequest request) {
        ensureProjectExists(projectId);

        boolean exists = projectMemberMapper.selectCount(
                Wrappers.<ProjectMember>lambdaQuery()
                        .eq(ProjectMember::getProjectId, projectId)
                        .eq(ProjectMember::getUserId, request.userId())
        ) > 0;
        if (exists) {
            throw new ProjectMemberAlreadyExistsException(projectId, request.userId());
        }

        ProjectMember member = new ProjectMember();
        member.setProjectId(projectId);
        member.setUserId(request.userId());
        member.setRole(request.role() != null ? request.role() : "MEMBER");
        projectMemberMapper.insert(member);
        return member;
    }

    /**
     * 查询指定项目下的所有成员列表，按 ID 升序排列。
     *
     * @param projectId 项目 ID
     * @return 项目成员实体列表
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
     * 根据项目 ID 和成员 ID 查询单个成员，不存在时抛出异常。
     *
     * @param projectId 项目 ID
     * @param memberId  成员记录 ID
     * @return 项目成员实体
     * @throws ProjectMemberNotFoundException 成员不存在时
     */
    public ProjectMember getByProjectAndId(Long projectId, Long memberId) {
        ensureProjectExists(projectId);
        ProjectMember member = projectMemberMapper.selectOne(Wrappers.<ProjectMember>lambdaQuery()
                .eq(ProjectMember::getProjectId, projectId)
                .eq(ProjectMember::getId, memberId)
                .last("LIMIT 1"));
        if (member == null) {
            throw new ProjectMemberNotFoundException(projectId, memberId);
        }
        return member;
    }

    /**
     * 校验项目是否存在，不存在时抛出 {@link com.aiplatform.backend.common.exception.ProjectNotFoundException}。
     *
     * @param projectId 项目 ID
     */
    private void ensureProjectExists(Long projectId) {
        if (projectMapper.selectById(projectId) == null) {
            throw new com.aiplatform.backend.common.exception.ProjectNotFoundException(projectId);
        }
    }
}
