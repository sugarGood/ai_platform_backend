package com.aiplatform.agent.gateway.controller;

import com.aiplatform.agent.gateway.service.KnowledgeIngestionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
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

    public KnowledgeController(KnowledgeIngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    /**
     * 从本地文件入库。
     *
     * @param kbId     知识库 ID
     * @param filePath 本地文件路径（如 F:\ai_file\编程规范.md）
     * @param docId    文档 ID
     * @param docTitle 文档标题
     * @return 入库结果
     */
    @PostMapping("/ingest")
    public ResponseEntity<Map<String, String>> ingestFile(
            @RequestParam Long kbId,
            @RequestParam String filePath,
            @RequestParam Long docId,
            @RequestParam String docTitle) {
        try {
            ingestionService.ingestFromFile(kbId, filePath, docId, docTitle);
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "文档入库完成"
            ));
        } catch (IOException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "文件读取失败: " + e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", "error",
                    "message", "入库失败: " + e.getMessage()
            ));
        }
    }
}
