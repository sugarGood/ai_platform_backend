package com.aiplatform.backend.controller;

import com.aiplatform.backend.common.exception.BizErrorCode;
import com.aiplatform.backend.common.exception.BusinessException;
import com.aiplatform.backend.dto.SaveMcpAuthorizationRequest;
import com.aiplatform.backend.entity.McpServer;
import com.aiplatform.backend.mapper.McpServerMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/** MCP 集成管理控制器（模块9）。 */
@RestController
@RequestMapping("/api/mcp-servers")
public class McpServerController {

    private final McpServerMapper mcpServerMapper;

    public McpServerController(McpServerMapper mcpServerMapper) {
        this.mcpServerMapper = mcpServerMapper;
    }

    @GetMapping
    public List<McpServer> list(@RequestParam(required = false) String serverType) {
        var q = Wrappers.<McpServer>lambdaQuery();
        if (serverType != null) q.eq(McpServer::getServerType, serverType);
        return mcpServerMapper.selectList(q.orderByDesc(McpServer::getId));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public McpServer create(@RequestBody McpServer body) {
        if (body.getStatus() == null) body.setStatus("ACTIVE");
        if (body.getAuthType() == null) body.setAuthType("NONE");
        mcpServerMapper.insert(body);
        return body;
    }

    @PutMapping("/{id}")
    public McpServer update(@PathVariable Long id, @RequestBody McpServer body) {
        McpServer e = mcpServerMapper.selectById(id);
        if (e == null) {
            throw new BusinessException(404, BizErrorCode.MCP_SERVER_NOT_FOUND, "MCP 服务不存在: " + id);
        }
        if (body.getDisplayName() != null) e.setDisplayName(body.getDisplayName());
        if (body.getServerUrl() != null) e.setServerUrl(body.getServerUrl());
        if (body.getAuthType() != null) e.setAuthType(body.getAuthType());
        if (body.getAuthConfig() != null) e.setAuthConfig(body.getAuthConfig());
        if (body.getCapabilities() != null) e.setCapabilities(body.getCapabilities());
        mcpServerMapper.updateById(e);
        return e;
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) { mcpServerMapper.deleteById(id); }

    @PostMapping("/{id}/test")
    public Map<String, Object> test(@PathVariable Long id) {
        McpServer s = mcpServerMapper.selectById(id);
        if (s == null) {
            throw new BusinessException(404, BizErrorCode.MCP_SERVER_NOT_FOUND, "MCP 服务不存在: " + id);
        }
        s.setLastCheckedAt(java.time.LocalDateTime.now());
        mcpServerMapper.updateById(s);
        return Map.of("serverId", id, "status", "pending", "message", "MCP连通性测试引擎待集成");
    }

    @PostMapping("/{id}/discover-tools")
    public Map<String, Object> discoverTools(@PathVariable Long id) {
        return Map.of("serverId", id, "tools", List.of(), "message", "工具发现引擎待集成");
    }

    /**
     * 兼容发现能力路径。
     */
    @PostMapping("/{id}/discover")
    public Map<String, Object> discover(@PathVariable Long id) {
        return discoverTools(id);
    }

    /**
     * 保存 MCP 授权状态。
     */
    @PostMapping("/{id}/authorize")
    public Map<String, Object> authorize(@PathVariable Long id,
                                         @RequestBody(required = false) SaveMcpAuthorizationRequest request) {
        McpServer server = mcpServerMapper.selectById(id);
        if (server == null) {
            throw new BusinessException(404, BizErrorCode.MCP_SERVER_NOT_FOUND, "MCP 服务不存在: " + id);
        }
        if (request != null && request.authConfig() != null) {
            server.setAuthConfig(request.authConfig());
            mcpServerMapper.updateById(server);
        }
        return Map.of(
                "serverId", id,
                "status", request != null && request.authStatus() != null ? request.authStatus() : "AUTHORIZED",
                "message", "授权状态已保存"
        );
    }
}
