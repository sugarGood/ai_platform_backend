package com.aiplatform.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 工具定义实体，对应 tool_definitions 表。
 *
 * <p>工具是平台 AI 能力扩展的重要方式，定义了工具的输入输出规范、实现方式和权限控制。
 * 支持内部实现、HTTP 回调和 MCP 代理等多种实现类型。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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

}
