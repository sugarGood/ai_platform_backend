package com.aiplatform.backend.service;

import com.aiplatform.backend.common.dto.PageResponse;
import com.aiplatform.backend.dto.CreateProjectRequest;
import com.aiplatform.backend.dto.ProjectResponse;
import com.aiplatform.backend.entity.Project;
import com.aiplatform.backend.mapper.ProjectMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 项目业务服务，提供项目的创建、查询和分页检索等核心操作。
 */
@Service
public class ProjectService {

    /** 项目数据访问 Mapper */
    private final ProjectMapper projectMapper;

    /**
     * 构造函数，注入项目 Mapper。
     *
     * @param projectMapper 项目数据访问接口
     */
    public ProjectService(ProjectMapper projectMapper) {
        this.projectMapper = projectMapper;
    }

    /**
     * 创建新项目并设置默认状态为 ACTIVE。
     *
     * @param request 创建项目的请求参数
     * @return 持久化后的项目实体
     */
    public Project create(CreateProjectRequest request) {
        Project project = new Project();
        project.setName(request.name());
        project.setCode(request.code());
        project.setDescription(request.description());
        project.setIcon(request.icon());
        project.setProjectType(request.projectType());
        project.setOwnerUserId(request.ownerUserId());
        project.setStatus("ACTIVE");
        projectMapper.insert(project);
        return project;
    }

    /**
     * 查询所有项目列表（不分页），按 ID 升序排列。
     *
     * @return 项目实体列表
     */
    public List<Project> list() {
        return projectMapper.selectList(Wrappers.<Project>lambdaQuery().orderByAsc(Project::getId));
    }

    /**
     * 分页查询项目列表，按 ID 升序排列。
     *
     * @param page 页码（从 1 开始）
     * @param size 每页记录数
     * @return 分页响应，包含项目 DTO 列表
     */
    public PageResponse<ProjectResponse> listPaged(int page, int size) {
        Page<Project> result = projectMapper.selectPage(
                new Page<>(page, size),
                Wrappers.<Project>lambdaQuery().orderByAsc(Project::getId)
        );
        return PageResponse.from(result, ProjectResponse::from);
    }

    /**
     * 根据 ID 查询项目，不存在时抛出 {@link com.aiplatform.backend.common.exception.ProjectNotFoundException}。
     *
     * @param projectId 项目 ID
     * @return 项目实体
     * @throws com.aiplatform.backend.common.exception.ProjectNotFoundException 项目不存在时
     */
    public Project getByIdOrThrow(Long projectId) {
        Project project = projectMapper.selectById(projectId);
        if (project == null) {
            throw new com.aiplatform.backend.common.exception.ProjectNotFoundException(projectId);
        }
        return project;
    }
}
