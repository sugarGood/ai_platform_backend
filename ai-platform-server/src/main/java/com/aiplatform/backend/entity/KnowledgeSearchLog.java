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
 * 知识库检索日志（只读），对应 {@code knowledge_search_logs} 表。
 *
 * <p>由网关在 RAG 检索后写入，管理端用于统计检索次数与命中率。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("knowledge_search_logs")
public class KnowledgeSearchLog {

    @TableId(type = IdType.AUTO)
    private Long id;
    /** 单次检索针对的知识库；跨库检索时可能为 null。 */
    private Long kbId;
    private Long projectId;
    private Long userId;
    private String query;
    /** 检索范围：GLOBAL / PROJECT / ALL。 */
    private String searchScope;
    /** 返回 chunk 条数；仪表盘用「大于 0」判定一次检索是否命中。 */
    private Integer resultCount;
    /** 命中文档 ID 列表（JSON 字符串，统计接口不解析）。 */
    private String hitDocIds;
    private BigDecimal relevanceScore;
    private Integer latencyMs;
    /** 来源：MANUAL_TEST / AI_AUTO / MCP_TOOL。 */
    private String source;
    /** 检索发生时间；按月统计口径依赖此字段。 */
    private LocalDateTime createdAt;
}
