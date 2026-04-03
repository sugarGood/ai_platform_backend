package com.aiplatform.backend.controller;

import com.aiplatform.backend.common.exception.BizErrorCode;
import com.aiplatform.backend.common.exception.BusinessException;
import com.aiplatform.backend.dto.EnableProjectToolRequest;
import com.aiplatform.backend.entity.ProjectTool;
import com.aiplatform.backend.service.ToolDefinitionService;
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
 * 项目工具管理控制器。
 *
 * <p>管理项目级工具的启用、查询和禁用，路径前缀为 {@code /api/projects/{projectId}/tools}。</p>
 */
@RestController
@RequestMapping("/api/projects/{projectId}/tools")
public class ProjectToolController {

    private final ToolDefinitionService toolDefinitionService;

    public ProjectToolController(ToolDefinitionService toolDefinitionService) {
        this.toolDefinitionService = toolDefinitionService;
    }

    /**
     * 为项目启用工具。
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectTool enable(@PathVariable Long projectId,
                              @RequestParam(required = false) Long toolId,
                              @Valid @RequestBody(required = false) EnableProjectToolRequest request) {
        Long resolvedToolId = toolId != null ? toolId : (request != null ? request.toolId() : null);
        if (resolvedToolId == null) {
            throw new BusinessException(400, BizErrorCode.VALIDATION_FAILED, "toolId 不能为空");
        }
        return toolDefinitionService.enableForProject(projectId, resolvedToolId);
    }

    /**
     * 查询项目已启用的工具列表。
     */
    @GetMapping
    public List<ProjectTool> list(@PathVariable Long projectId) {
        return toolDefinitionService.listProjectTools(projectId);
    }

    /**
     * 项目禁用（解绑）工具。
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void disable(@PathVariable Long projectId, @PathVariable Long id) {
        toolDefinitionService.disableForProject(projectId, id);
    }
}
