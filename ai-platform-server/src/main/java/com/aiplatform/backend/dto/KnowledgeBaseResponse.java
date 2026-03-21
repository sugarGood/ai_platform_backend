package com.aiplatform.backend.dto;

import com.aiplatform.backend.entity.KnowledgeBase;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 知识库响应 DTO。
 *
 * <p>用于向客户端返回知识库的详细信息，包括文档统计和命中率等运行指标。</p>
 *
 * @param id             知识库ID
 * @param name           知识库名称
 * @param description    知识库描述
 * @param scope          作用域
 * @param projectId      所属项目ID
 * @param category       分类
 * @param embeddingModel 向量化模型
 * @param docCount       文档数量
 * @param totalChunks    总知识块数
 * @param hitRate        命中率（百分比）
 * @param injectMode     注入模式
 * @param status         状态
 * @param createdBy      创建者ID
 * @param createdAt      创建时间
 */
public record KnowledgeBaseResponse(
        Long id,
        String name,
        String description,
        String scope,
        Long projectId,
        String category,
        String embeddingModel,
        Integer docCount,
        Integer totalChunks,
        BigDecimal hitRate,
        String injectMode,
        String status,
        Long createdBy,
        LocalDateTime createdAt
) {
    /**
     * 将知识库实体转换为响应 DTO。
     *
     * @param kb 知识库实体
     * @return 知识库响应 DTO
     */
    public static KnowledgeBaseResponse from(KnowledgeBase kb) {
        return new KnowledgeBaseResponse(
                kb.getId(), kb.getName(), kb.getDescription(), kb.getScope(),
                kb.getProjectId(), kb.getCategory(), kb.getEmbeddingModel(),
                kb.getDocCount(), kb.getTotalChunks(), kb.getHitRate(),
                kb.getInjectMode(), kb.getStatus(), kb.getCreatedBy(), kb.getCreatedAt()
        );
    }
}
