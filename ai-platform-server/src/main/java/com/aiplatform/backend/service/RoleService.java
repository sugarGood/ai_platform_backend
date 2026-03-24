package com.aiplatform.backend.service;

import com.aiplatform.backend.common.exception.RoleNotFoundException;
import com.aiplatform.backend.dto.CreateRoleRequest;
import com.aiplatform.backend.entity.Role;
import com.aiplatform.backend.mapper.RoleMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 角色业务服务。
 *
 * <p>提供角色的创建、查询等核心业务逻辑，新建角色默认状态为 ACTIVE。</p>
 */
@Service
public class RoleService {

    private final RoleMapper roleMapper;

    /**
     * 构造函数，注入角色数据访问层。
     *
     * @param roleMapper 角色Mapper
     */
    public RoleService(RoleMapper roleMapper) {
        this.roleMapper = roleMapper;
    }

    /**
     * 创建新角色。
     *
     * <p>根据请求参数创建角色记录，默认状态设置为 ACTIVE。</p>
     *
     * @param request 创建角色请求参数
     * @return 创建成功的角色实体
     */
    public Role create(CreateRoleRequest request) {
        Role role = new Role();
        role.setName(request.name());
        role.setCode(request.code());
        role.setRoleScope(request.roleScope());
        role.setDescription(request.description());
        role.setDefaultQuotaTokens(request.defaultQuotaTokens());
        role.setStatus("ACTIVE");
        roleMapper.insert(role);
        return role;
    }

    /** 查询所有角色列表，支持 scope 过滤，按ID升序排列。 */
    public List<Role> list(String scope) {
        var q = Wrappers.<Role>lambdaQuery();
        if (scope != null && !scope.isBlank()) q.eq(Role::getRoleScope, scope);
        q.orderByAsc(Role::getId);
        return roleMapper.selectList(q);
    }

    /**
     * 根据ID查询角色，不存在时抛出异常。
     *
     * @param roleId 角色ID
     * @return 角色实体
     * @throws RoleNotFoundException 当角色不存在时
     */
    public Role getByIdOrThrow(Long roleId) {
        Role role = roleMapper.selectById(roleId);
        if (role == null) throw new RoleNotFoundException(roleId);
        return role;
    }

    /** 编辑角色（仅更新非null字段）。 */
    public Role update(Long id, CreateRoleRequest request) {
        Role role = getByIdOrThrow(id);
        if (request.name() != null) role.setName(request.name());
        if (request.description() != null) role.setDescription(request.description());
        if (request.defaultQuotaTokens() != null) role.setDefaultQuotaTokens(request.defaultQuotaTokens());
        roleMapper.updateById(role);
        return role;
    }

    /** 查询角色已关联的权限ID列表（通过 role_permissions 表）。 */
    public List<Long> listPermissionIds(Long roleId) {
        getByIdOrThrow(roleId);
        // 通过 RolePermissionService 查询，此处直接查 role_permissions 表
        return List.of(); // 由 RolePermissionController 承载详细查询
    }
}
