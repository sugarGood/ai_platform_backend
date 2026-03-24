package com.aiplatform.backend.controller;

import com.aiplatform.backend.dto.CreateDepartmentRequest;
import com.aiplatform.backend.dto.DepartmentResponse;
import com.aiplatform.backend.service.DepartmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 部门管理控制器（完整版）。
 */
@RestController
@RequestMapping("/api/departments")
public class DepartmentController {

    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DepartmentResponse create(@Valid @RequestBody CreateDepartmentRequest request) {
        return DepartmentResponse.from(departmentService.create(request));
    }

    @GetMapping
    public List<DepartmentResponse> list() {
        return departmentService.list().stream().map(DepartmentResponse::from).toList();
    }

    @GetMapping("/{id}")
    public DepartmentResponse getById(@PathVariable Long id) {
        return DepartmentResponse.from(departmentService.getByIdOrThrow(id));
    }

    @PutMapping("/{id}")
    public DepartmentResponse update(@PathVariable Long id,
                                     @RequestBody CreateDepartmentRequest request) {
        return DepartmentResponse.from(departmentService.update(id, request));
    }
}
