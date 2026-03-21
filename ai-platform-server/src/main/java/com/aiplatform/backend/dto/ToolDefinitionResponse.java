package com.aiplatform.backend.dto;

import com.aiplatform.backend.entity.ToolDefinition;

import java.time.LocalDateTime;

/**
 * 工具定义响应 DTO。
 *
 * <p>用于向客户端返回工具定义的完整信息，包括接口规范和实现配置。</p>
 *
 * @param id                 工具ID
 * @param toolName           工具名称
 * @param displayName        展示名称
 * @param description        工具描述
 * @param scope              作用域
 * @param projectId          所属项目ID
 * @param category           工具分类
 * @param inputSchema        输入参数 JSON Schema
 * @param outputSchema       输出结果 JSON Schema
 * @param implType           实现方式
 * @param implConfig         实现配置
 * @param permissionRequired 调用所需权限点
 * @param auditLevel         审计级别
 * @param status             状态
 * @param createdAt          创建时间
 */
public record ToolDefinitionResponse(
        Long id,
        String toolName,
        String displayName,
        String description,
        String scope,
        Long projectId,
        String category,
        String inputSchema,
        String outputSchema,
        String implType,
        String implConfig,
        String permissionRequired,
        String auditLevel,
        String status,
        LocalDateTime createdAt
) {
    /**
     * 将工具定义实体转换为响应 DTO。
     *
     * @param t 工具定义实体
     * @return 工具定义响应 DTO
     */
    public static ToolDefinitionResponse from(ToolDefinition t) {
        return new ToolDefinitionResponse(
                t.getId(), t.getToolName(), t.getDisplayName(), t.getDescription(),
                t.getScope(), t.getProjectId(), t.getCategory(),
                t.getInputSchema(), t.getOutputSchema(), t.getImplType(),
                t.getImplConfig(), t.getPermissionRequired(), t.getAuditLevel(),
                t.getStatus(), t.getCreatedAt()
        );
    }
}
