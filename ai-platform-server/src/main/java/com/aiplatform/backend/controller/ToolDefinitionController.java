package com.aiplatform.backend.controller;

import com.aiplatform.backend.dto.CreateToolDefinitionRequest;
import com.aiplatform.backend.dto.ToolDefinitionResponse;
import com.aiplatform.backend.service.ToolDefinitionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 工具定义管理控制器（完整版）。
 *
 * <p>覆盖工具定义的完整 CRUD、禁用、测试和调用日志接口。</p>
 */
@RestController
@RequestMapping("/api/tools")
public class ToolDefinitionController {

    private final ToolDefinitionService toolDefinitionService;

    public ToolDefinitionController(ToolDefinitionService toolDefinitionService) {
        this.toolDefinitionService = toolDefinitionService;
    }

    /** 注册工具定义。 */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ToolDefinitionResponse create(@Valid @RequestBody CreateToolDefinitionRequest request) {
        return ToolDefinitionResponse.from(toolDefinitionService.create(request));
    }

    /** 查询工具定义列表，支持 scope 过滤。 */
    @GetMapping
    public List<ToolDefinitionResponse> list() {
        return toolDefinitionService.list().stream().map(ToolDefinitionResponse::from).toList();
    }

    /** 根据ID查询工具定义详情。 */
    @GetMapping("/{id}")
    public ToolDefinitionResponse getById(@PathVariable Long id) {
        return ToolDefinitionResponse.from(toolDefinitionService.getByIdOrThrow(id));
    }

    /** 编辑工具定义。 */
    @PutMapping("/{id}")
    public ToolDefinitionResponse update(@PathVariable Long id,
                                         @RequestBody CreateToolDefinitionRequest request) {
        return ToolDefinitionResponse.from(toolDefinitionService.update(id, request));
    }

    /** 禁用工具（status → INACTIVE）。 */
    @PostMapping("/{id}/disable")
    public ToolDefinitionResponse disable(@PathVariable Long id) {
        return ToolDefinitionResponse.from(toolDefinitionService.disable(id));
    }

    /**
     * 测试工具（发送测试请求验证可用性）。
     * TODO: 接入实际工具调用引擎。
     */
    @PostMapping("/{id}/test")
    public Map<String, Object> test(@PathVariable Long id,
                                    @RequestBody(required = false) Map<String, Object> input) {
        toolDefinitionService.getByIdOrThrow(id); // 校验存在
        return Map.of(
                "toolId", id,
                "status", "pending",
                "message", "工具测试引擎待集成"
        );
    }

    /**
     * 查询工具调用日志。
     * TODO: 接入 tool_invocation_logs 表。
     */
    @GetMapping("/{id}/invocation-logs")
    public Map<String, Object> invocationLogs(
            @PathVariable Long id,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "1") int page,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "20") int size) {
        toolDefinitionService.getByIdOrThrow(id);
        return Map.of(
                "toolId", id,
                "items", java.util.List.of(),
                "total", 0,
                "page", page,
                "size", size
        );
    }
}
