package com.aiplatform.backend.controller;

import com.aiplatform.backend.dto.CreateRoleRequest;
import com.aiplatform.backend.dto.RoleResponse;
import com.aiplatform.backend.service.RoleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 角色管理控制器。
 *
 * <p>提供角色的创建、列表查询、详情查询等 REST API 接口，
 * 基础路径为 /api/admin/roles。</p>
 */
@RestController
@RequestMapping("/api/admin/roles")
public class RoleController {

    private final RoleService roleService;

    /**
     * 构造函数，注入角色业务服务。
     *
     * @param roleService 角色服务
     */
    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    /**
     * 创建新角色。
     *
     * @param request 创建角色请求参数
     * @return 创建成功的角色响应
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RoleResponse create(@Valid @RequestBody CreateRoleRequest request) {
        return RoleResponse.from(roleService.create(request));
    }

    /**
     * 查询所有角色列表。
     *
     * @return 角色响应列表
     */
    @GetMapping
    public List<RoleResponse> list() {
        return roleService.list().stream()
                .map(RoleResponse::from)
                .toList();
    }

    /**
     * 根据ID查询角色详情。
     *
     * @param id 角色ID
     * @return 角色响应
     */
    @GetMapping("/{id}")
    public RoleResponse getById(@PathVariable Long id) {
        return RoleResponse.from(roleService.getByIdOrThrow(id));
    }
}
