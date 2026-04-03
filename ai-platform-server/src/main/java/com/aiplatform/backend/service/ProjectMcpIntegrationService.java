package com.aiplatform.backend.service;

import com.aiplatform.backend.common.exception.BizErrorCode;
import com.aiplatform.backend.common.exception.BusinessException;
import com.aiplatform.backend.entity.McpServer;
import com.aiplatform.backend.entity.ProjectMcpIntegration;
import com.aiplatform.backend.mapper.McpServerMapper;
import com.aiplatform.backend.mapper.ProjectMcpIntegrationMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 项目 MCP 集成服务。
 */
@Service
public class ProjectMcpIntegrationService {

    private final ProjectMcpIntegrationMapper projectMcpIntegrationMapper;
    private final McpServerMapper mcpServerMapper;

    public ProjectMcpIntegrationService(ProjectMcpIntegrationMapper projectMcpIntegrationMapper,
                                        McpServerMapper mcpServerMapper) {
        this.projectMcpIntegrationMapper = projectMcpIntegrationMapper;
        this.mcpServerMapper = mcpServerMapper;
    }

    /**
     * 查询项目已启用的 MCP 集成。
     */
    public List<ProjectMcpIntegration> listByProject(Long projectId) {
        return projectMcpIntegrationMapper.selectList(Wrappers.<ProjectMcpIntegration>lambdaQuery()
                .eq(ProjectMcpIntegration::getProjectId, projectId)
                .orderByAsc(ProjectMcpIntegration::getId));
    }

    /**
     * 启用 MCP 集成到项目。
     */
    public ProjectMcpIntegration enableForProject(Long projectId, Long mcpServerId) {
        McpServer server = mcpServerMapper.selectById(mcpServerId);
        if (server == null) {
            throw new BusinessException(404, BizErrorCode.MCP_SERVER_NOT_FOUND, "MCP 服务不存在: " + mcpServerId);
        }

        ProjectMcpIntegration integration = new ProjectMcpIntegration();
        integration.setProjectId(projectId);
        integration.setMcpServerId(mcpServerId);
        integration.setStatus("ACTIVE");
        projectMcpIntegrationMapper.insert(integration);
        return integration;
    }

    /**
     * 移除项目 MCP 集成。
     */
    public void disableForProject(Long projectId, Long integrationIdOrMcpServerId) {
        projectMcpIntegrationMapper.delete(Wrappers.<ProjectMcpIntegration>lambdaQuery()
                .eq(ProjectMcpIntegration::getProjectId, projectId)
                .and(w -> w.eq(ProjectMcpIntegration::getId, integrationIdOrMcpServerId)
                        .or()
                        .eq(ProjectMcpIntegration::getMcpServerId, integrationIdOrMcpServerId)));
    }
}
