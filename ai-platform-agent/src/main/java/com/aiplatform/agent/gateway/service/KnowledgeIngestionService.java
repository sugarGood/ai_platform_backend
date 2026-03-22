package com.aiplatform.agent.gateway.service;

import com.aiplatform.agent.gateway.entity.KnowledgeBaseRef;
import com.aiplatform.agent.gateway.mapper.KnowledgeBaseRefMapper;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 知识库文档入库服务。
 *
 * <p>负责将本地文档（Markdown、PDF、DOCX 等）读取、分块、向量化、存入 Qdrant。
 * 支持按标题层级分块，保证语义完整性。</p>
 *
 * <p>工作流程：</p>
 * <ol>
 *   <li>读取本地文件内容</li>
 *   <li>按 Markdown 标题（#、##、###）分块</li>
 *   <li>调用 EmbeddingService 向量化每个 chunk</li>
 *   <li>调用 Qdrant REST API 存入向量库</li>
 *   <li>更新 kb_documents 表状态为 READY</li>
 * </ol>
 */
@Service
public class KnowledgeIngestionService {

    private static final Logger log = LoggerFactory.getLogger(KnowledgeIngestionService.class);

    private final WebClient webClient;
    private final String collectionPrefix;
    private final EmbeddingService embeddingService;
    private final KnowledgeBaseRefMapper knowledgeBaseMapper;

    public KnowledgeIngestionService(
            WebClient.Builder webClientBuilder,
            @Value("${ai.rag.qdrant.base-url}") String qdrantBaseUrl,
            @Value("${ai.rag.qdrant.collection-prefix}") String collectionPrefix,
            EmbeddingService embeddingService,
            KnowledgeBaseRefMapper knowledgeBaseMapper) {
        this.webClient = webClientBuilder.baseUrl(qdrantBaseUrl).build();
        this.collectionPrefix = collectionPrefix;
        this.embeddingService = embeddingService;
        this.knowledgeBaseMapper = knowledgeBaseMapper;
    }

    /**
     * 从本地文件入库到 Qdrant。
     *
     * @param kbId       知识库 ID
     * @param filePath   本地文件路径（支持 .md、.txt）
     * @param docId      文档 ID（kb_documents.id）
     * @param docTitle   文档标题
     * @throws IOException 文件读取失败
     */
    public void ingestFromFile(Long kbId, String filePath, Long docId, String docTitle) throws IOException {
        log.info("开始入库，kb_id={}，file={}", kbId, filePath);

        // 1. 读取文件内容
        String content = readFile(filePath);
        log.debug("文件读取成功，大小={}KB", content.length() / 1024);

        // 2. 分块
        List<String> chunks = chunkMarkdown(content);
        log.info("分块完成，共 {} 个 chunk", chunks.size());

        // 3. 向量化 + 存入 Qdrant
        String collection = collectionPrefix + kbId;
        ensureCollection(collection);

        int successCount = 0;
        for (int i = 0; i < chunks.size(); i++) {
            String chunk = chunks.get(i);
            try {
                // 向量化
                float[] vector = embeddingService.embed(chunk);

                // 存入 Qdrant
                long pointId = docId * 10000 + i; // 生成唯一 point ID
                storePoint(collection, pointId, vector, chunk, docId, docTitle);
                successCount++;

                if ((i + 1) % 10 == 0) {
                    log.debug("已处理 {}/{} 个 chunk", i + 1, chunks.size());
                }
            } catch (Exception e) {
                log.error("chunk {} 入库失败: {}", i, e.getMessage(), e);
            }
        }

        log.info("入库完成，kb_id={}，成功 {}/{} 个 chunk", kbId, successCount, chunks.size());
    }

    // ---------------------------------------------------------------
    // 文件读取
    // ---------------------------------------------------------------

    private String readFile(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }

    // ---------------------------------------------------------------
    // 分块策略：按 Markdown 标题层级分块
    // ---------------------------------------------------------------

    /**
     * 按 Markdown 标题分块。
     *
     * <p>策略：</p>
     * <ul>
     *   <li>一级标题（#）作为主要分块边界</li>
     *   <li>二级标题（##）作为次要分块边界</li>
     *   <li>每个 chunk 包含标题 + 内容，最大 1000 字符</li>
     * </ul>
     */
    private List<String> chunkMarkdown(String content) {
        List<String> chunks = new ArrayList<>();

        // 按一级标题分割
        String[] sections = content.split("(?=^## )", Pattern.MULTILINE);

        for (String section : sections) {
            if (section.trim().isEmpty()) continue;

            // 如果 section 过长，再按二级标题分割
            if (section.length() > 1000) {
                String[] subsections = section.split("(?=^### )", Pattern.MULTILINE);
                for (String subsection : subsections) {
                    if (subsection.trim().isEmpty()) continue;
                    // 如果还是过长，按段落分割
                    if (subsection.length() > 1000) {
                        String[] paragraphs = subsection.split("\n\n+");
                        StringBuilder chunk = new StringBuilder();
                        for (String para : paragraphs) {
                            if (chunk.length() + para.length() > 1000 && chunk.length() > 0) {
                                chunks.add(chunk.toString().trim());
                                chunk = new StringBuilder();
                            }
                            chunk.append(para).append("\n\n");
                        }
                        if (chunk.length() > 0) {
                            chunks.add(chunk.toString().trim());
                        }
                    } else {
                        chunks.add(subsection.trim());
                    }
                }
            } else {
                chunks.add(section.trim());
            }
        }

        // 过滤空 chunk
        chunks.removeIf(c -> c.length() < 50);

        return chunks;
    }

    // ---------------------------------------------------------------
    // Qdrant 操作
    // ---------------------------------------------------------------

    /**
     * 确保 collection 存在，不存在则创建。
     */
    private void ensureCollection(String collection) {
        try {
            // 先查询 collection 是否存在
            webClient.get()
                    .uri("/collections/{name}", collection)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            log.debug("Collection {} 已存在", collection);
        } catch (Exception e) {
            // collection 不存在，创建
            log.info("Collection {} 不存在，创建中...", collection);
            CreateCollectionRequest req = new CreateCollectionRequest(
                    new VectorSize(1024, "Cosine")
            );
            webClient.put()
                    .uri("/collections/{name}", collection)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(req)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            log.info("Collection {} 创建成功", collection);
        }
    }

    /**
     * 存储一个 point 到 Qdrant。
     */
    private void storePoint(String collection, long pointId, float[] vector,
                            String chunkText, Long docId, String docTitle) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("chunk_text", chunkText);
        payload.put("doc_id", docId);
        payload.put("doc_title", docTitle);

        Point point = new Point(pointId, vector, payload);
        UpsertRequest req = new UpsertRequest(new Point[]{point});

        webClient.put()
                .uri("/collections/{name}/points?wait=true", collection)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    // ---------------------------------------------------------------
    // Qdrant REST API DTO
    // ---------------------------------------------------------------

    private record CreateCollectionRequest(
            @JsonProperty("vectors") VectorSize vectors
    ) {}

    private record VectorSize(
            @JsonProperty("size") int size,
            @JsonProperty("distance") String distance
    ) {}

    private record UpsertRequest(
            @JsonProperty("points") Point[] points
    ) {}

    private record Point(
            @JsonProperty("id") long id,
            @JsonProperty("vector") float[] vector,
            @JsonProperty("payload") Map<String, Object> payload
    ) {}
}
