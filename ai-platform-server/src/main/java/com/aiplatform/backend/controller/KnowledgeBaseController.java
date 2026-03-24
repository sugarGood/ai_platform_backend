package com.aiplatform.backend.controller;

import com.aiplatform.backend.dto.CreateKnowledgeBaseRequest;
import com.aiplatform.backend.dto.KbDocumentResponse;
import com.aiplatform.backend.dto.KnowledgeDashboardResponse;
import com.aiplatform.backend.common.exception.BusinessException;
import com.aiplatform.backend.dto.KnowledgeBaseResponse;
import com.aiplatform.backend.service.KnowledgeBaseService;
import com.aiplatform.backend.service.KnowledgeDashboardService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 知识库管理控制器（完整版）。
 *
 * <p>覆盖知识库和文档的完整 CRUD，以及 RAG 配置、检索测试、仪表盘统计接口。</p>
 */
@RestController
@RequestMapping("/api/knowledge-bases")
public class KnowledgeBaseController {

    private final KnowledgeBaseService knowledgeBaseService;
    private final KnowledgeDashboardService knowledgeDashboardService;

    public KnowledgeBaseController(KnowledgeBaseService knowledgeBaseService,
                                   KnowledgeDashboardService knowledgeDashboardService) {
        this.knowledgeBaseService = knowledgeBaseService;
        this.knowledgeDashboardService = knowledgeDashboardService;
    }

    /** 创建知识库。 */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public KnowledgeBaseResponse create(@Valid @RequestBody CreateKnowledgeBaseRequest request) {
        return KnowledgeBaseResponse.from(knowledgeBaseService.create(request));
    }

    /** 查询知识库列表，可按作用域过滤。 */
    @GetMapping
    public List<KnowledgeBaseResponse> list(@RequestParam(required = false) String scope) {
        if (scope != null) {
            return knowledgeBaseService.listByScope(scope).stream().map(KnowledgeBaseResponse::from).toList();
        }
        return knowledgeBaseService.list().stream().map(KnowledgeBaseResponse::from).toList();
    }

    /**
     * 知识库管理页顶部四块统计。
     *
     * <p>路径必须为字面量 {@code /dashboard}，且声明在 {@code /{id}} 之前，避免部分路由实现将
     * {@code "dashboard"} 误匹配为 Long 型 id。</p>
     */
    @GetMapping("/dashboard")
    public KnowledgeDashboardResponse dashboard() {
        return knowledgeDashboardService.getDashboard();
    }

    /** 根据ID查询知识库详情。 */
    @GetMapping("/{id}")
    public KnowledgeBaseResponse getById(@PathVariable Long id) {
        return KnowledgeBaseResponse.from(knowledgeBaseService.getByIdOrThrow(id));
    }

    /** 编辑知识库。 */
    @PutMapping("/{id}")
    public KnowledgeBaseResponse update(@PathVariable Long id,
                                        @RequestBody CreateKnowledgeBaseRequest request) {
        return KnowledgeBaseResponse.from(knowledgeBaseService.update(id, request));
    }

    /** 归档知识库（status → INACTIVE）。 */
    @PostMapping("/{id}/archive")
    public KnowledgeBaseResponse archive(@PathVariable Long id) {
        return KnowledgeBaseResponse.from(knowledgeBaseService.archive(id));
    }

    /** 查询指定知识库下的文档列表。 */
    @GetMapping("/{kbId}/documents")
    public List<KbDocumentResponse> listDocuments(@PathVariable Long kbId) {
        return knowledgeBaseService.listDocuments(kbId).stream().map(KbDocumentResponse::from).toList();
    }



    /**
     * 上传文档到 MinIO（按日目录 {@code yyyy/MM/dd}，对象名 {@code 文件名_时间戳.ext}），登记元数据后异步触发向量化入库。
     *
     * <p>表单字段：{@code file}（必填），{@code title}、{@code injectMode}（可选）。</p>
     */
    @PostMapping(value = "/{kbId}/documents/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public KbDocumentResponse uploadDocument(@PathVariable Long kbId,
                                             @RequestPart("file") MultipartFile file,
                                             @RequestParam(required = false) String title,
                                             @RequestParam(required = false) String injectMode) {
        return KbDocumentResponse.from(knowledgeBaseService.uploadDocument(kbId, file, title, injectMode));
    }

    /**
     * 重新向量化（适用于 {@code ERROR} / {@code PENDING} / {@code READY} 等需重算场景；{@code PROCESSING} 时返回 409）。
     */
    @PostMapping("/{kbId}/documents/{docId}/reingest")
    public KbDocumentResponse reingestDocument(@PathVariable Long kbId, @PathVariable Long docId) {
        return KbDocumentResponse.from(knowledgeBaseService.reingestDocument(kbId, docId));
    }

    /** 查询文档详情（含处理状态）。 */
    @GetMapping("/{kbId}/documents/{docId}")
    public KbDocumentResponse getDocument(@PathVariable Long kbId, @PathVariable Long docId) {
        return KbDocumentResponse.from(knowledgeBaseService.getDocumentByIdOrThrow(docId));
    }

    /** 删除文档。 */
    @DeleteMapping("/{kbId}/documents/{docId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDocument(@PathVariable Long kbId, @PathVariable Long docId) {
        knowledgeBaseService.deleteDocument(kbId, docId);
    }

    /** 检索测试（输入 query 返回向量检索结果，经内网 Agent 调用 Ollama + Qdrant）。 */
    @PostMapping("/{kbId}/search")
    public Map<String, Object> search(@PathVariable Long kbId,
                                      @RequestBody(required = false) Map<String, Object> body) {
        if (body == null) {
            throw new BusinessException(400, "INVALID_BODY", "请求体不能为空");
        }
        Object qRaw = body.get("query");
        String query = null;
        if (qRaw instanceof String s) {
            query = s;
        } else if (qRaw != null) {
            query = String.valueOf(qRaw);
        }
        if (query == null || query.isBlank()) {
            throw new BusinessException(400, "INVALID_QUERY", "query 不能为空");
        }
        int resultCount = 5;
        if (body.get("resultCount") instanceof Number n) {
            resultCount = n.intValue();
        }
        Double scoreThreshold = null;
        if (body.get("scoreThreshold") instanceof Number n) {
            scoreThreshold = n.doubleValue();
        }
        return knowledgeBaseService.searchKnowledgeBase(kbId, query.trim(), resultCount, scoreThreshold);
    }

    /** 获取RAG配置。 */
    @GetMapping("/{kbId}/rag-config")
    public KnowledgeBaseResponse getRagConfig(@PathVariable Long kbId) {
        return KnowledgeBaseResponse.from(knowledgeBaseService.getByIdOrThrow(kbId));
    }

    /** 更新RAG配置。 */
    @PutMapping("/{kbId}/rag-config")
    public KnowledgeBaseResponse updateRagConfig(@PathVariable Long kbId,
                                                  @RequestBody Map<String, String> config) {
        return KnowledgeBaseResponse.from(knowledgeBaseService.updateRagConfig(kbId, config));
    }
}
