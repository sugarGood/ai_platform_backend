package com.aiplatform.backend.service;

import com.aiplatform.backend.entity.KbDocument;
import com.aiplatform.backend.mapper.KbDocumentMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Map;

/**
 * 文档保存到共享存储后，异步请求 Agent 完成分块与向量入库，并回写 {@code kb_documents} 状态。
 */
@Service
public class KnowledgeIngestAsyncService {

    private static final Logger log = LoggerFactory.getLogger(KnowledgeIngestAsyncService.class);

    private static final int ERROR_MSG_MAX = 500;

    private final WebClient agentWebClient;
    private final KbDocumentMapper kbDocumentMapper;

    public KnowledgeIngestAsyncService(@Qualifier("agentWebClient") WebClient agentWebClient,
                                       KbDocumentMapper kbDocumentMapper) {
        this.agentWebClient = agentWebClient;
        this.kbDocumentMapper = kbDocumentMapper;
    }

    /**
     * 异步触发向量化；与上传请求解耦，失败时仅记录日志并更新文档为 ERROR。
     */
    /**
     * @param minioObjectKey MinIO 对象键（与 {@code kb_documents.file_path} 一致）
     */
    @Async("knowledgeIngestExecutor")
    public void runIngest(Long kbId, Long docId, String docTitle, String minioObjectKey) {
        kbDocumentMapper.update(null, Wrappers.<KbDocument>lambdaUpdate()
                .set(KbDocument::getStatus, "PROCESSING")
                .set(KbDocument::getErrorMessage, null)
                .eq(KbDocument::getId, docId));

        try {
            MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
            form.add("kbId", String.valueOf(kbId));
            form.add("docId", String.valueOf(docId));
            form.add("docTitle", docTitle != null ? docTitle : "");
            form.add("objectKey", minioObjectKey);
            // 与仅支持本地路径的旧版 Agent 兼容：参数存在即可，实际以 objectKey 为准
            form.add("filePath", "");

            Map<String, Object> body = agentWebClient.post()
                    .uri("/api/knowledge/ingest")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(BodyInserters.fromFormData(form))
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .block();

            int chunkCount = extractChunkCount(body);
            kbDocumentMapper.update(null, Wrappers.<KbDocument>lambdaUpdate()
                    .set(KbDocument::getStatus, "READY")
                    .set(KbDocument::getChunkCount, chunkCount)
                    .eq(KbDocument::getId, docId));
            log.info("知识库文档向量化完成 docId={} chunkCount={}", docId, chunkCount);
        } catch (WebClientResponseException e) {
            String msg = "Agent 入库失败 HTTP " + e.getStatusCode().value() + ": "
                    + truncate(e.getResponseBodyAsString(), ERROR_MSG_MAX);
            markError(docId, msg);
            log.warn(msg, e);
        } catch (Exception e) {
            String msg = truncate("向量化调用失败: " + e.getMessage(), ERROR_MSG_MAX);
            markError(docId, msg);
            log.warn(msg, e);
        }
    }

    private void markError(Long docId, String message) {
        kbDocumentMapper.update(null, Wrappers.<KbDocument>lambdaUpdate()
                .set(KbDocument::getStatus, "ERROR")
                .set(KbDocument::getErrorMessage, message)
                .eq(KbDocument::getId, docId));
    }

    private static int extractChunkCount(Map<String, Object> body) {
        if (body == null) {
            return 0;
        }
        Object c = body.get("chunkCount");
        if (c instanceof Number n) {
            return n.intValue();
        }
        return 0;
    }

    private static String truncate(String s, int max) {
        if (s == null) {
            return "";
        }
        return s.length() <= max ? s : s.substring(0, max);
    }
}
