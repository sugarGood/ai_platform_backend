package com.aiplatform.agent.gateway.controller;

import com.aiplatform.agent.gateway.service.EmbeddingService;
import com.aiplatform.agent.gateway.service.KnowledgeIngestionService;
import com.aiplatform.agent.gateway.service.KnowledgeRetrievalService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 知识库管理接口。
 *
 * <p>提供文档入库、检索等管理功能。</p>
 */
@RestController
@RequestMapping("/api/knowledge")
public class KnowledgeController {

    private final KnowledgeIngestionService ingestionService;
    private final KnowledgeRetrievalService retrievalService;

    public KnowledgeController(KnowledgeIngestionService ingestionService,
                               KnowledgeRetrievalService retrievalService) {
        this.ingestionService = ingestionService;
        this.retrievalService = retrievalService;
    }

    /**
     * 向量检索测试：请求体 {@code { "kbId": 1, "query": "...", "resultCount": 5, "scoreThreshold": 0.25 }}，
     * {@code resultCount}、{@code scoreThreshold} 可选。
     */
    @PostMapping("/search")
    public ResponseEntity<Map<String, Object>> search(@RequestBody Map<String, Object> body) {
        if (body == null) {
            return badRequest("请求体不能为空");
        }
        Object kbIdObj = body.get("kbId");
        if (!(kbIdObj instanceof Number)) {
            return badRequest("kbId 必填且为数字");
        }
        long kbId = ((Number) kbIdObj).longValue();
        Object qObj = body.get("query");
        String query = qObj == null ? null : String.valueOf(qObj);
        int resultCount = 5;
        Object rc = body.get("resultCount");
        if (rc instanceof Number n) {
            resultCount = n.intValue();
        }
        Double scoreThreshold = null;
        Object st = body.get("scoreThreshold");
        if (st instanceof Number n) {
            scoreThreshold = n.doubleValue();
        }
        try {
            Map<String, Object> data = retrievalService.search(kbId, query, resultCount, scoreThreshold);
            return ResponseEntity.ok(data);
        } catch (IllegalArgumentException e) {
            return badRequest(e.getMessage());
        } catch (EmbeddingService.EmbeddingException e) {
            Map<String, Object> err = new LinkedHashMap<>();
            err.put("message", "向量化失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(err);
        }
    }

    private static ResponseEntity<Map<String, Object>> badRequest(String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("message", message);
        return ResponseEntity.badRequest().body(body);
    }

    /**
     * 文档入库：二选一 {@code objectKey}（MinIO 对象键）或 {@code filePath}（本机路径）。
     */
    @PostMapping("/ingest")
    public ResponseEntity<Map<String, Object>> ingestFile(
            @RequestParam Long kbId,
            @RequestParam(required = false, defaultValue = "") String filePath,
            @RequestParam(required = false, defaultValue = "") String objectKey,
            @RequestParam Long docId,
            @RequestParam String docTitle) {
        try {
            int chunkCount;
            if (objectKey != null && !objectKey.isBlank()) {
                chunkCount = ingestionService.ingestFromMinioObject(objectKey.strip(), kbId, docId, docTitle);
            } else if (filePath != null && !filePath.isBlank()) {
                chunkCount = ingestionService.ingestFromFile(kbId, filePath, docId, docTitle);
            } else {
                Map<String, Object> body = new LinkedHashMap<>();
                body.put("status", "error");
                body.put("message", "objectKey 与 filePath 至少填写一个");
                return ResponseEntity.badRequest().body(body);
            }
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("status", "success");
            body.put("message", "文档入库完成");
            body.put("chunkCount", chunkCount);
            return ResponseEntity.ok(body);
        } catch (IOException e) {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("status", "error");
            body.put("message", "文件读取失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(body);
        } catch (Exception e) {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("status", "error");
            body.put("message", "入库失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(body);
        }
    }
}
