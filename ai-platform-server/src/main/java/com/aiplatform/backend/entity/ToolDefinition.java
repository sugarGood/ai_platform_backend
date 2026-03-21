package com.aiplatform.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * 工具定义实体，对应 tool_definitions 表。
 *
 * <p>工具是平台 AI 能力扩展的重要方式，定义了工具的输入输出规范、实现方式和权限控制。
 * 支持内部实现、HTTP 回调和 MCP 代理等多种实现类型。</p>
 */
@TableName("tool_definitions")
public class ToolDefinition {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 工具名称（AI 调用时使用的标识符） */
    private String toolName;

    /** 展示名称（界面显示用） */
    private String displayName;

    /** 工具描述 */
    private String description;

    /** 作用域：BUILTIN（内置）/ GLOBAL（全局）/ PROJECT（项目级） */
    private String scope;

    /** 所属项目ID，scope 为 PROJECT 时有效 */
    private Long projectId;

    /** 工具分类 */
    private String category;

    /** 输入参数 JSON Schema */
    private String inputSchema;

    /** 输出结果 JSON Schema */
    private String outputSchema;

    /** 实现方式：INTERNAL（内部）/ HTTP_CALLBACK（HTTP 回调）/ MCP_PROXY（MCP 代理） */
    private String implType;

    /** 实现配置（JSON 格式），存储不同实现方式的具体配置 */
    private String implConfig;

    /** 调用所需的权限点标识 */
    private String permissionRequired;

    /** 审计级别：NORMAL（普通）/ SENSITIVE（敏感）/ CRITICAL（关键） */
    private String auditLevel;

    /** 状态：ACTIVE / INACTIVE */
    private String status;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 最后更新时间 */
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getToolName() { return toolName; }
    public void setToolName(String toolName) { this.toolName = toolName; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getScope() { return scope; }
    public void setScope(String scope) { this.scope = scope; }
    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getInputSchema() { return inputSchema; }
    public void setInputSchema(String inputSchema) { this.inputSchema = inputSchema; }
    public String getOutputSchema() { return outputSchema; }
    public void setOutputSchema(String outputSchema) { this.outputSchema = outputSchema; }
    public String getImplType() { return implType; }
    public void setImplType(String implType) { this.implType = implType; }
    public String getImplConfig() { return implConfig; }
    public void setImplConfig(String implConfig) { this.implConfig = implConfig; }
    public String getPermissionRequired() { return permissionRequired; }
    public void setPermissionRequired(String permissionRequired) { this.permissionRequired = permissionRequired; }
    public String getAuditLevel() { return auditLevel; }
    public void setAuditLevel(String auditLevel) { this.auditLevel = auditLevel; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
