package com.aiplatform.backend.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 创建工具定义请求 DTO。
 *
 * @param toolName           工具名称，AI 调用时使用的标识符（必填）
 * @param displayName        展示名称（必填）
 * @param description        工具描述
 * @param scope              作用域：BUILTIN / GLOBAL / PROJECT（必填）
 * @param projectId          所属项目ID，scope 为 PROJECT 时需指定
 * @param category           工具分类
 * @param inputSchema        输入参数 JSON Schema（必填）
 * @param outputSchema       输出结果 JSON Schema
 * @param implType           实现方式：INTERNAL / HTTP_CALLBACK / MCP_PROXY，默认 INTERNAL
 * @param implConfig         实现配置（JSON）
 * @param permissionRequired 调用所需权限点
 * @param auditLevel         审计级别：NORMAL / SENSITIVE / CRITICAL，默认 NORMAL
 */
public record CreateToolDefinitionRequest(
        @NotBlank String toolName,
        @NotBlank String displayName,
        String description,
        @NotBlank String scope,
        Long projectId,
        String category,
        @NotBlank String inputSchema,
        String outputSchema,
        String implType,
        String implConfig,
        String permissionRequired,
        String auditLevel
) {
}
