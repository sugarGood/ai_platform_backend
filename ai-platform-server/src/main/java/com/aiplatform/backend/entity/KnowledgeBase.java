package com.aiplatform.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 知识库实体，对应 knowledge_bases 表。
 *
 * <p>知识库是平台知识管理的核心载体，支持全局共享和项目级私有两种作用域。
 * 每个知识库可包含多个文档，文档经向量化后用于 AI 检索增强（RAG）。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("knowledge_bases")
public class KnowledgeBase {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 知识库名称 */
    private String name;

    /** 知识库描述 */
    private String description;

    /** 作用域：GLOBAL（全局共享）/ PROJECT（项目私有） */
    private String scope;

    /** 所属项目ID，当 scope 为 PROJECT 时有效 */
    private Long projectId;

    /** 知识库分类 */
    private String category;

    /** 向量化模型名称，如 bge-m3 */
    private String embeddingModel;

    /** 文档数量 */
    private Integer docCount;

    /** 总知识块（chunk）数 */
    private Integer totalChunks;

    /** 检索命中率（百分比） */
    private BigDecimal hitRate;

    /** 注入模式：AUTO_INJECT（自动注入）/ ON_DEMAND（按需注入）/ DISABLED（禁用） */
    private String injectMode;

    /** 状态：ACTIVE / INACTIVE */
    private String status;

    /** 创建者用户ID */
    private Long createdBy;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 最后更新时间 */
    private LocalDateTime updatedAt;

}
