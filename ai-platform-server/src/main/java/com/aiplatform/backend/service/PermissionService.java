package com.aiplatform.backend.service;

import com.aiplatform.backend.common.exception.PermissionNotFoundException;
import com.aiplatform.backend.dto.CreatePermissionRequest;
import com.aiplatform.backend.entity.PermissionDefinition;
import com.aiplatform.backend.mapper.PermissionDefinitionMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 权限点业务服务。
 *
 * <p>提供权限点定义的创建、查询等核心业务逻辑。</p>
 */
@Service
public class PermissionService {

    private final PermissionDefinitionMapper permissionDefinitionMapper;

    /**
     * 构造函数，注入权限点定义数据访问层。
     *
     * @param permissionDefinitionMapper 权限点定义Mapper
     */
    public PermissionService(PermissionDefinitionMapper permissionDefinitionMapper) {
        this.permissionDefinitionMapper = permissionDefinitionMapper;
    }

    /**
     * 创建新的权限点定义。
     *
     * @param request 创建权限点请求参数
     * @return 创建成功的权限点定义实体
     */
    public PermissionDefinition create(CreatePermissionRequest request) {
        PermissionDefinition permission = new PermissionDefinition();
        permission.setModule(request.module());
        permission.setPermissionKey(request.permissionKey());
        permission.setName(request.name());
        permission.setDescription(request.description());
        permission.setPermissionScope(request.permissionScope());
        permissionDefinitionMapper.insert(permission);
        return permission;
    }

    /**
     * 查询所有权限点定义列表，按ID升序排列。
     *
     * @return 权限点定义列表
     */
    public List<PermissionDefinition> list() {
        return permissionDefinitionMapper.selectList(
                Wrappers.<PermissionDefinition>lambdaQuery().orderByAsc(PermissionDefinition::getId)
        );
    }

    /**
     * 根据ID查询权限点定义，不存在时抛出异常。
     *
     * @param permissionId 权限点ID
     * @return 权限点定义实体
     * @throws PermissionNotFoundException 当权限点不存在时
     */
    public PermissionDefinition getByIdOrThrow(Long permissionId) {
        PermissionDefinition permission = permissionDefinitionMapper.selectById(permissionId);
        if (permission == null) {
            throw new PermissionNotFoundException(permissionId);
        }
        return permission;
    }
}
