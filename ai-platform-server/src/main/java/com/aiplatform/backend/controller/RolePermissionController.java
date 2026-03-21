package com.aiplatform.backend.controller;

import com.aiplatform.backend.dto.AssignRolePermissionRequest;
import com.aiplatform.backend.dto.RolePermissionResponse;
import com.aiplatform.backend.service.RolePermissionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 角色-权限关联管理控制器。
 *
 * <p>提供角色权限的分配、按角色查询和删除等 REST API 接口，
 * 基础路径为 /api/admin/role-permissions。</p>
 */
@RestController
@RequestMapping("/api/admin/role-permissions")
public class RolePermissionController {

    private final RolePermissionService rolePermissionService;

    /**
     * 构造函数，注入角色-权限关联业务服务。
     *
     * @param rolePermissionService 角色-权限关联服务
     */
    public RolePermissionController(RolePermissionService rolePermissionService) {
        this.rolePermissionService = rolePermissionService;
    }

    /**
     * 为角色分配权限。
     *
     * @param request 分配角色权限请求参数
     * @return 创建成功的角色-权限关联响应
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RolePermissionResponse assign(@Valid @RequestBody AssignRolePermissionRequest request) {
        return RolePermissionResponse.from(rolePermissionService.assign(request));
    }

    /**
     * 查询指定角色的所有权限关联。
     *
     * @param roleId 角色ID
     * @return 该角色的权限关联响应列表
     */
    @GetMapping
    public List<RolePermissionResponse> listByRoleId(@RequestParam Long roleId) {
        return rolePermissionService.listByRoleId(roleId).stream()
                .map(RolePermissionResponse::from)
                .toList();
    }

    /**
     * 删除角色-权限关联记录。
     *
     * @param id 关联记录ID
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        rolePermissionService.delete(id);
    }
}
