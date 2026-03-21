package com.aiplatform.backend.controller;

import com.aiplatform.backend.dto.CreateServiceRequest;
import com.aiplatform.backend.dto.ServiceResponse;
import com.aiplatform.backend.service.ServiceEntityService;
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
 * 项目服务管理控制器，提供项目下服务的创建和列表查询接口。
 *
 * <p>API 基路径：{@code /api/projects/{projectId}/services}</p>
 */
@RestController
@RequestMapping("/api/projects/{projectId}/services")
public class ProjectServiceController {

    /** 项目服务业务类 */
    private final ServiceEntityService serviceEntityService;

    /**
     * 构造函数，注入项目服务业务类。
     *
     * @param serviceEntityService 项目服务业务类
     */
    public ProjectServiceController(ServiceEntityService serviceEntityService) {
        this.serviceEntityService = serviceEntityService;
    }


    /**
     * 在指定项目下创建服务。
     *
     * @param projectId 项目 ID（路径参数）
     * @param request   创建服务的请求参数
     * @return 新创建的服务响应 DTO
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ServiceResponse create(@PathVariable Long projectId, @Valid @RequestBody CreateServiceRequest request) {
        return ServiceResponse.from(serviceEntityService.create(projectId, request));
    }


    /**
     * 查询指定项目下的服务列表。
     *
     * @param projectId 项目 ID（路径参数）
     * @return 服务响应 DTO 列表
     */
    @GetMapping
    public List<ServiceResponse> list(@PathVariable Long projectId) {
        return serviceEntityService.listByProjectId(projectId).stream()
                .map(ServiceResponse::from)
                .toList();
    }
}
