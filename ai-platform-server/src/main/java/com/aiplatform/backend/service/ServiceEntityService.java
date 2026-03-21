package com.aiplatform.backend.service;

import com.aiplatform.backend.dto.CreateServiceRequest;
import com.aiplatform.backend.entity.ServiceEntity;
import com.aiplatform.backend.mapper.ProjectMapper;
import com.aiplatform.backend.mapper.ServiceEntityMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 项目服务业务类，提供服务的创建和列表查询等操作。
 */
@Service
public class ServiceEntityService {

    /** 项目数据访问 Mapper */
    private final ProjectMapper projectMapper;
    /** 服务数据访问 Mapper */
    private final ServiceEntityMapper serviceEntityMapper;

    /**
     * 构造函数，注入所需的 Mapper。
     *
     * @param projectMapper       项目数据访问接口
     * @param serviceEntityMapper 服务数据访问接口
     */
    public ServiceEntityService(ProjectMapper projectMapper, ServiceEntityMapper serviceEntityMapper) {
        this.projectMapper = projectMapper;
        this.serviceEntityMapper = serviceEntityMapper;
    }

    /**
     * 在指定项目下创建服务，默认状态为 ACTIVE，默认主分支为 "main"。
     *
     * @param projectId 项目 ID
     * @param request   创建服务的请求参数
     * @return 持久化后的服务实体
     */
    public ServiceEntity create(Long projectId, CreateServiceRequest request) {
        ensureProjectExists(projectId);

        ServiceEntity entity = new ServiceEntity();
        entity.setProjectId(projectId);
        entity.setName(request.name());
        entity.setDescription(request.description());
        entity.setGitRepoUrl(request.gitRepoUrl());
        entity.setMainBranch(request.mainBranch() != null ? request.mainBranch() : "main");
        entity.setFramework(request.framework());
        entity.setLanguage(request.language());
        entity.setStatus("ACTIVE");
        serviceEntityMapper.insert(entity);
        return entity;
    }

    /**
     * 查询指定项目下的所有服务列表，按 ID 升序排列。
     *
     * @param projectId 项目 ID
     * @return 服务实体列表
     */
    public List<ServiceEntity> listByProjectId(Long projectId) {
        ensureProjectExists(projectId);
        return serviceEntityMapper.selectList(
                Wrappers.<ServiceEntity>lambdaQuery()
                        .eq(ServiceEntity::getProjectId, projectId)
                        .orderByAsc(ServiceEntity::getId)
        );
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
