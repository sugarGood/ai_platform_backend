package com.aiplatform.agent.gateway.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 知识库向量检索（控制台「检索测试」与对内 HTTP 接口共用）。
 *
 * <p>流程：query → {@link EmbeddingService} → {@link QdrantSearchService}，
 * 与对话侧 RAG 使用同一套 Qdrant collection 与阈值配置。</p>
 */
@Service
public class KnowledgeRetrievalService {

    private final EmbeddingService embeddingService;
    private final QdrantSearchService qdrantSearchService;
    private final KnowledgeSearchLogService searchLogService;
    private final double defaultScoreThreshold;

    public KnowledgeRetrievalService(
            EmbeddingService embeddingService,
            QdrantSearchService qdrantSearchService,
            KnowledgeSearchLogService searchLogService,
            @Value("${ai.rag.search.score-threshold}") double defaultScoreThreshold) {
        this.embeddingService = embeddingService;
        this.qdrantSearchService = qdrantSearchService;
        this.searchLogService = searchLogService;
        this.defaultScoreThreshold = defaultScoreThreshold;
    }

    /**
     * @param resultCount       期望返回条数，≤0 时默认 5
     * @param scoreThreshold    为 null 使用全局配置；可传 0.2～0.5 等以适配短 Query
     */
    public Map<String, Object> search(Long kbId, String query, int resultCount, Double scoreThreshold) {
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("query 不能为空");
        }
        String q = query.trim();
        int limit = resultCount > 0 ? resultCount : 5;
        double appliedThr = scoreThreshold != null
                ? Math.min(1.0, Math.max(0.0, scoreThreshold))
                : defaultScoreThreshold;

        long t0 = System.currentTimeMillis();
        float[] vector = embeddingService.embed(q);
        List<QdrantSearchService.ChunkResult> chunks = qdrantSearchService.search(kbId, vector, limit, scoreThreshold);
        long latencyMs = System.currentTimeMillis() - t0;

        searchLogService.logAsync(kbId, null, null, q, chunks, latencyMs, "MANUAL_TEST");

        List<Map<String, Object>> results = new ArrayList<>(chunks.size());
        for (QdrantSearchService.ChunkResult c : chunks) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("docId", c.docId());
            row.put("docTitle", c.docTitle());
            row.put("chunkText", c.chunkText());
            row.put("score", c.score());
            results.add(row);
        }

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("kbId", kbId);
        out.put("query", q);
        out.put("resultCount", results.size());
        out.put("latencyMs", latencyMs);
        out.put("appliedScoreThreshold", appliedThr);
        out.put("results", results);
        return out;
    }
}
