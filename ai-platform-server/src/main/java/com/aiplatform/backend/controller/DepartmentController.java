package com.aiplatform.backend.controller;

import com.aiplatform.backend.dto.CreateDepartmentRequest;
import com.aiplatform.backend.dto.DepartmentResponse;
import com.aiplatform.backend.service.DepartmentService;
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
 * 部门管理控制器。
 * <p>提供 {@code /api/departments} 下的 REST 端点，支持部门创建和查询操作。</p>
 */
@RestController
@RequestMapping("/api/departments")
public class DepartmentController {

    private final DepartmentService departmentService;

    /**
     * 构造方法，注入部门业务服务。
     *
     * @param departmentService 部门服务
     */
    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    /**
     * 创建新部门。
     *
     * @param request 创建部门请求参数
     * @return 新创建的部门响应
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DepartmentResponse create(@Valid @RequestBody CreateDepartmentRequest request) {
        return DepartmentResponse.from(departmentService.create(request));
    }

    /**
     * 查询全部部门列表。
     *
     * @return 部门响应列表
     */
    @GetMapping
    public List<DepartmentResponse> list() {
        return departmentService.list().stream().map(DepartmentResponse::from).toList();
    }

    /**
     * 根据ID查询部门详情。
     *
     * @param id 部门ID
     * @return 部门响应
     */
    @GetMapping("/{id}")
    public DepartmentResponse getById(@PathVariable Long id) {
        return DepartmentResponse.from(departmentService.getByIdOrThrow(id));
    }
}
