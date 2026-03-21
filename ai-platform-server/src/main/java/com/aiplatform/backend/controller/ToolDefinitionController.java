package com.aiplatform.backend.controller;

import com.aiplatform.backend.dto.CreateToolDefinitionRequest;
import com.aiplatform.backend.dto.ToolDefinitionResponse;
import com.aiplatform.backend.entity.ProjectTool;
import com.aiplatform.backend.service.ToolDefinitionService;
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
 * 工具定义管理控制器。
 *
 * <p>提供工具定义的创建和查询等 REST API，路径前缀为 {@code /api/tools}。</p>
 */
@RestController
@RequestMapping("/api/tools")
public class ToolDefinitionController {

    private final ToolDefinitionService toolDefinitionService;

    /**
     * 构造函数，注入工具定义业务服务。
     *
     * @param toolDefinitionService 工具定义业务服务
     */
    public ToolDefinitionController(ToolDefinitionService toolDefinitionService) {
        this.toolDefinitionService = toolDefinitionService;
    }

    /**
     * 创建工具定义。
     *
     * @param request 创建工具定义请求体
     * @return 新创建的工具定义响应
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ToolDefinitionResponse create(@Valid @RequestBody CreateToolDefinitionRequest request) {
        return ToolDefinitionResponse.from(toolDefinitionService.create(request));
    }

    /**
     * 查询所有工具定义列表。
     *
     * @return 工具定义响应列表
     */
    @GetMapping
    public List<ToolDefinitionResponse> list() {
        return toolDefinitionService.list().stream().map(ToolDefinitionResponse::from).toList();
    }

    /**
     * 根据ID查询工具定义详情。
     *
     * @param id 工具定义ID
     * @return 工具定义响应
     */
    @GetMapping("/{id}")
    public ToolDefinitionResponse getById(@PathVariable Long id) {
        return ToolDefinitionResponse.from(toolDefinitionService.getByIdOrThrow(id));
    }
}
