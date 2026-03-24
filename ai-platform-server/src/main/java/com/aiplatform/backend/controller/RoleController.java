package com.aiplatform.backend.controller;

import com.aiplatform.backend.dto.CreateRoleRequest;
import com.aiplatform.backend.dto.RoleResponse;
import com.aiplatform.backend.service.RoleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 角色管理控制器（完整版）。
 */
@RestController
@RequestMapping("/api/admin/roles")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    /** 创建新角色。 */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RoleResponse create(@Valid @RequestBody CreateRoleRequest request) {
        return RoleResponse.from(roleService.create(request));
    }

    /** 查询角色列表，支持 scope 过滤。 */
    @GetMapping
    public List<RoleResponse> list(@RequestParam(required = false) String scope) {
        return roleService.list(scope).stream().map(RoleResponse::from).toList();
    }

    /** 根据ID查询角色详情。 */
    @GetMapping("/{id}")
    public RoleResponse getById(@PathVariable Long id) {
        return RoleResponse.from(roleService.getByIdOrThrow(id));
    }

    /** 编辑角色。 */
    @PutMapping("/{id}")
    public RoleResponse update(@PathVariable Long id,
                               @RequestBody CreateRoleRequest request) {
        return RoleResponse.from(roleService.update(id, request));
    }
}
