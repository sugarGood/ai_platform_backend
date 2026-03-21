package com.aiplatform.backend.service;

import com.aiplatform.backend.dto.AssignRolePermissionRequest;
import com.aiplatform.backend.entity.RolePermission;
import com.aiplatform.backend.mapper.RolePermissionMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 角色-权限关联业务服务。
 *
 * <p>提供角色与权限点之间关联关系的分配、查询和删除操作。</p>
 */
@Service
public class RolePermissionService {

    private final RolePermissionMapper rolePermissionMapper;

    /**
     * 构造函数，注入角色-权限关联数据访问层。
     *
     * @param rolePermissionMapper 角色-权限关联Mapper
     */
    public RolePermissionService(RolePermissionMapper rolePermissionMapper) {
        this.rolePermissionMapper = rolePermissionMapper;
    }

    /**
     * 为角色分配权限。
     *
     * <p>创建角色与权限点的关联记录，并指定访问级别。</p>
     *
     * @param request 分配角色权限请求参数
     * @return 创建成功的角色-权限关联实体
     */
    public RolePermission assign(AssignRolePermissionRequest request) {
        RolePermission rp = new RolePermission();
        rp.setRoleId(request.roleId());
        rp.setPermissionId(request.permissionId());
        rp.setAccessLevel(request.accessLevel());
        rolePermissionMapper.insert(rp);
        return rp;
    }

    /**
     * 查询指定角色的所有权限关联，按ID升序排列。
     *
     * @param roleId 角色ID
     * @return 该角色的权限关联列表
     */
    public List<RolePermission> listByRoleId(Long roleId) {
        return rolePermissionMapper.selectList(
                Wrappers.<RolePermission>lambdaQuery()
                        .eq(RolePermission::getRoleId, roleId)
                        .orderByAsc(RolePermission::getId)
        );
    }

    /**
     * 删除角色-权限关联记录。
     *
     * @param id 关联记录ID
     */
    public void delete(Long id) {
        rolePermissionMapper.deleteById(id);
    }
}
