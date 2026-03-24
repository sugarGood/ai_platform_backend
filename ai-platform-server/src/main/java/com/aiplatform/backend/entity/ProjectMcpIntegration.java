package com.aiplatform.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 项目MCP集成关联实体，对应 project_mcp_integrations 表。 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("project_mcp_integrations")
public class ProjectMcpIntegration {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long projectId;
    private Long mcpServerId;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
