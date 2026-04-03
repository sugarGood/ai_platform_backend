package com.aiplatform.backend.controller;

import com.aiplatform.backend.common.exception.BizErrorCode;
import com.aiplatform.backend.common.exception.BusinessException;
import com.aiplatform.backend.dto.EnableProjectMcpIntegrationRequest;
import com.aiplatform.backend.entity.ProjectMcpIntegration;
import com.aiplatform.backend.service.ProjectMcpIntegrationService;
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
 * 项目 MCP 集成管理控制器。
 */
@RestController
@RequestMapping("/api/projects/{projectId}/mcp-integrations")
public class ProjectMcpIntegrationController {

    private final ProjectMcpIntegrationService projectMcpIntegrationService;

    public ProjectMcpIntegrationController(ProjectMcpIntegrationService projectMcpIntegrationService) {
        this.projectMcpIntegrationService = projectMcpIntegrationService;
    }

    /**
     * 查询项目集成列表。
     */
    @GetMapping
    public List<ProjectMcpIntegration> list(@PathVariable Long projectId) {
        return projectMcpIntegrationService.listByProject(projectId);
    }

    /**
     * 启用 MCP 集成到项目。
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectMcpIntegration create(@PathVariable Long projectId,
                                        @RequestParam(required = false) Long mcpServerId,
                                        @Valid @RequestBody(required = false) EnableProjectMcpIntegrationRequest request) {
        Long resolvedMcpServerId = mcpServerId != null ? mcpServerId : (request != null ? request.mcpServerId() : null);
        if (resolvedMcpServerId == null) {
            throw new BusinessException(400, BizErrorCode.VALIDATION_FAILED, "mcpServerId 不能为空");
        }
        return projectMcpIntegrationService.enableForProject(projectId, resolvedMcpServerId);
    }

    /**
     * 移除项目集成。
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long projectId, @PathVariable Long id) {
        projectMcpIntegrationService.disableForProject(projectId, id);
    }
}
