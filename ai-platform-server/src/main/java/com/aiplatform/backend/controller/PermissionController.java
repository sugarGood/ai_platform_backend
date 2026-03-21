package com.aiplatform.backend.controller;

import com.aiplatform.backend.dto.CreatePermissionRequest;
import com.aiplatform.backend.dto.PermissionResponse;
import com.aiplatform.backend.service.PermissionService;
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
 * 权限管理控制器。
 *
 * <p>提供权限点定义的创建、列表查询、详情查询等 REST API 接口，
 * 基础路径为 /api/admin/permissions。</p>
 */
@RestController
@RequestMapping("/api/admin/permissions")
public class PermissionController {

    private final PermissionService permissionService;

    /**
     * 构造函数，注入权限点业务服务。
     *
     * @param permissionService 权限点服务
     */
    public PermissionController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    /**
     * 创建新的权限点定义。
     *
     * @param request 创建权限点请求参数
     * @return 创建成功的权限点响应
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PermissionResponse create(@Valid @RequestBody CreatePermissionRequest request) {
        return PermissionResponse.from(permissionService.create(request));
    }

    /**
     * 查询所有权限点定义列表。
     *
     * @return 权限点响应列表
     */
    @GetMapping
    public List<PermissionResponse> list() {
        return permissionService.list().stream()
                .map(PermissionResponse::from)
                .toList();
    }

    /**
     * 根据ID查询权限点详情。
     *
     * @param id 权限点ID
     * @return 权限点响应
     */
    @GetMapping("/{id}")
    public PermissionResponse getById(@PathVariable Long id) {
        return PermissionResponse.from(permissionService.getByIdOrThrow(id));
    }
}
