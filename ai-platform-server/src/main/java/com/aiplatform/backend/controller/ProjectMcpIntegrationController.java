package com.aiplatform.backend.controller;

import com.aiplatform.backend.entity.ProjectMcpIntegration;
import com.aiplatform.backend.mapper.ProjectMcpIntegrationMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** 项目MCP集成管理控制器。 */
@RestController
@RequestMapping("/api/projects/{projectId}/mcp-integrations")
public class ProjectMcpIntegrationController {

    private final ProjectMcpIntegrationMapper mapper;

    public ProjectMcpIntegrationController(ProjectMcpIntegrationMapper mapper) {
        this.mapper = mapper;
    }

    @GetMapping
    public List<ProjectMcpIntegration> list(@PathVariable Long projectId) {
        return mapper.selectList(Wrappers.<ProjectMcpIntegration>lambdaQuery()
                .eq(ProjectMcpIntegration::getProjectId, projectId));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectMcpIntegration create(@PathVariable Long projectId,
                                        @RequestBody ProjectMcpIntegration body) {
        body.setProjectId(projectId);
        if (body.getStatus() == null) body.setStatus("ACTIVE");
        mapper.insert(body);
        return body;
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long projectId, @PathVariable Long id) {
        mapper.delete(Wrappers.<ProjectMcpIntegration>lambdaQuery()
                .eq(ProjectMcpIntegration::getProjectId, projectId)
                .eq(ProjectMcpIntegration::getId, id));
    }
}
