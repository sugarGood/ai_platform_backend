package com.aiplatform.agent.gateway.service;

import com.aiplatform.agent.gateway.entity.KnowledgeSearchLogRef;
import com.aiplatform.agent.gateway.mapper.KnowledgeSearchLogRefMapper;
import com.aiplatform.agent.gateway.service.QdrantSearchService.ChunkResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 知识库检索日志服务。
 *
 * <p>在每次 RAG 向量检索完成后，异步写入 {@code knowledge_search_logs} 表。
 * 使用 {@code @Async} 避免日志写入阻塞主流程，确保对话响应时延不受影响。</p>
 */
@Service
public class KnowledgeSearchLogService {

    private static final Logger log = LoggerFactory.getLogger(KnowledgeSearchLogService.class);

    private final KnowledgeSearchLogRefMapper logMapper;

    public KnowledgeSearchLogService(KnowledgeSearchLogRefMapper logMapper) {
        this.logMapper = logMapper;
    }

    /**
     * 异步记录一次 RAG 检索日志。
     *
     * @param kbId      知识库 ID
     * @param projectId 项目 ID
     * @param userId    用户 ID
     * @param query     用户原始问题
     * @param chunks    检索命中的 chunk 列表
     * @param latencyMs 检索耗时（毫秒）
     */
    @Async
    public void logAsync(Long kbId, Long projectId, Long userId,
                         String query, List<ChunkResult> chunks, long latencyMs) {
        try {
            // 命中的文档 ID 列表序列化为 JSON 数组字符串，如 [1,3,5]
            String hitDocIds = null;
            BigDecimal relevanceScore = null;
            if (!chunks.isEmpty()) {
                hitDocIds = "[" + chunks.stream()
                        .filter(c -> c.docId() != null)
                        .map(c -> String.valueOf(c.docId()))
                        .distinct()
                        .collect(Collectors.joining(",")) + "]";

                // 取所有命中 chunk 中的最高相似度分数
                double maxScore = chunks.stream()
                        .mapToDouble(ChunkResult::score)
                        .max()
                        .orElse(0.0);
                relevanceScore = BigDecimal.valueOf(maxScore)
                        .setScale(4, RoundingMode.HALF_UP);
            }

            KnowledgeSearchLogRef entry = KnowledgeSearchLogRef.builder()
                    .kbId(kbId)
                    .projectId(projectId)
                    .userId(userId)
                    .query(query)
                    .searchScope(projectId != null ? "PROJECT" : "GLOBAL")
                    .resultCount(chunks.size())
                    .latencyMs((int) latencyMs)
                    .source("AI_AUTO")
                    .createdAt(LocalDateTime.now())
                    .hitDocIds(hitDocIds)
                    .relevanceScore(relevanceScore)
                    .build();

            logMapper.insert(entry);
        } catch (Exception e) {
            // 日志写入失败不影响主流程
            log.warn("知识库检索日志写入失败，kb_id={}: {}", kbId, e.getMessage());
        }
    }
}
