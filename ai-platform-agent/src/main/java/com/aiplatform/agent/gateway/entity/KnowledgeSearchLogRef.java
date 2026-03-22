package com.aiplatform.agent.gateway.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 知识库检索日志实体（网关写入）。
 *
 * <p>映射 {@code knowledge_search_logs} 表，记录每次 RAG 向量检索的详细信息，
 * 用于知识库命中率统计和检索质量分析。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("knowledge_search_logs")
public class KnowledgeSearchLogRef {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long kbId;
    private Long projectId;
    private Long userId;
    private String query;
    private String searchScope;
    private Integer resultCount;
    private String hitDocIds;
    private java.math.BigDecimal relevanceScore;
    private Integer latencyMs;
    private String source;
    private java.time.LocalDateTime createdAt;
}
