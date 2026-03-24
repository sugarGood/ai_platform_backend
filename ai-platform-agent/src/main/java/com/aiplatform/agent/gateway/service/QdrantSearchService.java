package com.aiplatform.agent.gateway.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Qdrant 向量检索服务。
 *
 * <p>基于用户问题的 embedding 向量，在 Qdrant 对应知识库 collection 中检索
 * 语义最相关的文档片段（chunk），用于 RAG 上下文注入。</p>
 *
 * <p>命名规则：每个知识库（knowledge_bases.id）对应一个 Qdrant collection，
 * collection 名 = collectionPrefix + kb_id，例如 kb_id=1 → "kb_1"。</p>
 *
 * <p>Qdrant Point payload 约定字段：</p>
 * <pre>
 * {
 *   "chunk_text":  "文档片段原文",
 *   "doc_id":      文档ID（kb_documents.id）,
 *   "doc_title":   "文档标题",
 *   "kb_id":       知识库ID
 * }
 * </pre>
 */
@Service
public class QdrantSearchService {

    private static final Logger log = LoggerFactory.getLogger(QdrantSearchService.class);

    private final WebClient webClient;
    private final String collectionPrefix;
    private final int topK;
    private final double scoreThreshold;
    private final int maxChunkChars;

    public QdrantSearchService(
            WebClient.Builder webClientBuilder,
            @Value("${ai.rag.qdrant.base-url}") String qdrantBaseUrl,
            @Value("${ai.rag.qdrant.collection-prefix}") String collectionPrefix,
            @Value("${ai.rag.search.top-k}") int topK,
            @Value("${ai.rag.search.score-threshold}") double scoreThreshold,
            @Value("${ai.rag.search.max-chunk-chars}") int maxChunkChars) {
        this.webClient = webClientBuilder.baseUrl(qdrantBaseUrl).build();
        this.collectionPrefix = collectionPrefix;
        this.topK = topK;
        this.scoreThreshold = scoreThreshold;
        this.maxChunkChars = maxChunkChars;
    }

    /**
     * 检索与问题最相关的文档片段（条数使用配置项 {@code ai.rag.search.top-k}）。
     */
    public List<ChunkResult> search(Long kbId, float[] queryVector) {
        return search(kbId, queryVector, topK);
    }

    /**
     * 检索与问题最相关的文档片段。
     *
     * @param kbId        知识库 ID（对应 knowledge_bases.id）
     * @param queryVector 问题的 embedding 向量
     * @param limit       返回条数上限，会被限制在 1～100 之间
     * @return 检索结果列表，已按相似度降序排列，低于阈值的已过滤
     */
    public List<ChunkResult> search(Long kbId, float[] queryVector, int limit) {
        return search(kbId, queryVector, limit, null);
    }

    /**
     * @param scoreThresholdOverride 为 null 时使用配置 {@code ai.rag.search.score-threshold}；短词/英文词可传更低值（如 0.25）提升召回
     */
    public List<ChunkResult> search(Long kbId, float[] queryVector, int limit, Double scoreThresholdOverride) {
        int effLimit = limit > 0 ? Math.min(Math.max(limit, 1), 100) : topK;
        double thr = scoreThresholdOverride != null
                ? Math.min(1.0, Math.max(0.0, scoreThresholdOverride))
                : scoreThreshold;
        // 多取候选再在应用层按阈值截断，避免「只取 Top5 但分数都略低于阈值」时结果为空（常见于短 Query）
        int fetchLimit = Math.min(128, Math.max(effLimit * 10, 20));

        String collection = collectionPrefix + kbId;
        log.debug("Qdrant 检索，collection={}, returnLimit={}, fetchLimit={}, scoreThreshold={}",
                collection, effLimit, fetchLimit, thr);

        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("vector", queryVector);
        requestBody.put("limit", fetchLimit);
        requestBody.put("with_payload", true);
        // 使用 Qdrant 文档约定的扁平数字字段；此前嵌套对象会导致阈值未生效或行为异常

        try {
            SearchResponse response = webClient.post()
                    .uri("/collections/{collection}/points/search", collection)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(SearchResponse.class)
                    .block();

            if (response == null || response.result() == null) {
                return Collections.emptyList();
            }

            List<ChunkResult> ranked = new ArrayList<>();
            for (SearchPoint p : response.result()) {
                if (p.score() < thr) {
                    continue;
                }
                String text = extractPayloadString(p.payload(), "chunk_text");
                if (text != null && text.length() > maxChunkChars) {
                    text = text.substring(0, maxChunkChars) + "...";
                }
                ranked.add(new ChunkResult(
                        kbId,
                        extractPayloadLong(p.payload(), "doc_id"),
                        extractPayloadString(p.payload(), "doc_title"),
                        text,
                        p.score()
                ));
                if (ranked.size() >= effLimit) {
                    break;
                }
            }
            return ranked;

        } catch (WebClientResponseException.NotFound e) {
            // collection 不存在（向量尚未入库），静默返回空，不影响正常对话
            log.warn("Qdrant collection 不存在，kb_id={}，collection={}。向量尚未入库，跳过 RAG 注入。",
                    kbId, collection);
            return Collections.emptyList();
        } catch (Exception e) {
            // 向量库不可用时降级：不注入 chunk，不影响对话
            log.error("Qdrant 检索失败，kb_id={}，降级跳过 RAG 注入: {}", kbId, e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * 检索结果：一个相关文档片段。
     *
     * @param kbId      知识库 ID
     * @param docId     文档 ID
     * @param docTitle  文档标题
     * @param chunkText 片段文本内容
     * @param score     相似度分数（0~1，越高越相关）
     */
    public record ChunkResult(
            Long kbId,
            Long docId,
            String docTitle,
            String chunkText,
            double score
    ) {}

    // ---------------------------------------------------------------
    // Qdrant REST API 响应 DTO
    // ---------------------------------------------------------------

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record SearchResponse(
            @JsonProperty("result") List<SearchPoint> result
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record SearchPoint(
            @JsonProperty("id") Object id,
            @JsonProperty("score") double score,
            @JsonProperty("payload") Map<String, Object> payload
    ) {}

    // ---------------------------------------------------------------
    // Payload 字段提取工具方法
    // ---------------------------------------------------------------

    private String extractPayloadString(Map<String, Object> payload, String key) {
        if (payload == null) return null;
        Object val = payload.get(key);
        return val != null ? val.toString() : null;
    }

    private Long extractPayloadLong(Map<String, Object> payload, String key) {
        if (payload == null) return null;
        Object val = payload.get(key);
        if (val instanceof Number n) return n.longValue();
        if (val instanceof String s) { try { return Long.parseLong(s); } catch (NumberFormatException ignored) {} }
        return null;
    }
}
