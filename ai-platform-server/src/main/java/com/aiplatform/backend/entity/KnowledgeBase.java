package com.aiplatform.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 知识库实体，对应 knowledge_bases 表。
 *
 * <p>知识库是平台知识管理的核心载体，支持全局共享和项目级私有两种作用域。
 * 每个知识库可包含多个文档，文档经向量化后用于 AI 检索增强（RAG）。</p>
 */
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

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getScope() { return scope; }
    public void setScope(String scope) { this.scope = scope; }
    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getEmbeddingModel() { return embeddingModel; }
    public void setEmbeddingModel(String embeddingModel) { this.embeddingModel = embeddingModel; }
    public Integer getDocCount() { return docCount; }
    public void setDocCount(Integer docCount) { this.docCount = docCount; }
    public Integer getTotalChunks() { return totalChunks; }
    public void setTotalChunks(Integer totalChunks) { this.totalChunks = totalChunks; }
    public BigDecimal getHitRate() { return hitRate; }
    public void setHitRate(BigDecimal hitRate) { this.hitRate = hitRate; }
    public String getInjectMode() { return injectMode; }
    public void setInjectMode(String injectMode) { this.injectMode = injectMode; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
