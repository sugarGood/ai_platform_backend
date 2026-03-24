package com.aiplatform.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** MCP Server 实体，对应 mcp_servers 表。 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("mcp_servers")
public class McpServer {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String serverName;
    private String displayName;
    private String description;
    private String serverType;    // BUILTIN / OFFICIAL / ENTERPRISE / PROJECT
    private Long projectId;
    private String category;
    private String serverUrl;
    private String authType;      // NONE / BEARER / OAUTH2 / API_KEY
    private String authConfig;    // JSON
    private String capabilities;  // JSON
    private String status;        // ACTIVE / INACTIVE
    private LocalDateTime lastCheckedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
