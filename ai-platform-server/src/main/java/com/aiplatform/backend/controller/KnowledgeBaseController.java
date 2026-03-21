package com.aiplatform.backend.controller;

import com.aiplatform.backend.dto.CreateKbDocumentRequest;
import com.aiplatform.backend.dto.CreateKnowledgeBaseRequest;
import com.aiplatform.backend.dto.KbDocumentResponse;
import com.aiplatform.backend.dto.KnowledgeBaseResponse;
import com.aiplatform.backend.entity.ProjectKnowledgeConfig;
import com.aiplatform.backend.service.KnowledgeBaseService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

/**
 * 知识库管理控制器。
 *
 * <p>提供知识库和知识库文档的 REST API，路径前缀为 {@code /api/knowledge-bases}。</p>
 */
@RestController
@RequestMapping("/api/knowledge-bases")
public class KnowledgeBaseController {

    private final KnowledgeBaseService knowledgeBaseService;

    /**
     * 构造函数，注入知识库业务服务。
     *
     * @param knowledgeBaseService 知识库业务服务
     */
    public KnowledgeBaseController(KnowledgeBaseService knowledgeBaseService) {
        this.knowledgeBaseService = knowledgeBaseService;
    }

    /**
     * 创建知识库。
     *
     * @param request 创建知识库请求体
     * @return 新创建的知识库响应
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public KnowledgeBaseResponse create(@Valid @RequestBody CreateKnowledgeBaseRequest request) {
        return KnowledgeBaseResponse.from(knowledgeBaseService.create(request));
    }

    /**
     * 查询知识库列表，可按作用域过滤。
     *
     * @param scope 可选的作用域过滤参数：GLOBAL / PROJECT
     * @return 知识库响应列表
     */
    @GetMapping
    public List<KnowledgeBaseResponse> list(@RequestParam(required = false) String scope) {
        if (scope != null) {
            return knowledgeBaseService.listByScope(scope).stream().map(KnowledgeBaseResponse::from).toList();
        }
        return knowledgeBaseService.list().stream().map(KnowledgeBaseResponse::from).toList();
    }

    /**
     * 根据ID查询知识库详情。
     *
     * @param id 知识库ID
     * @return 知识库响应
     */
    @GetMapping("/{id}")
    public KnowledgeBaseResponse getById(@PathVariable Long id) {
        return KnowledgeBaseResponse.from(knowledgeBaseService.getByIdOrThrow(id));
    }

    /**
     * 查询指定知识库下的文档列表。
     *
     * @param kbId 知识库ID
     * @return 文档响应列表
     */
    @GetMapping("/{kbId}/documents")
    public List<KbDocumentResponse> listDocuments(@PathVariable Long kbId) {
        return knowledgeBaseService.listDocuments(kbId).stream().map(KbDocumentResponse::from).toList();
    }

    /**
     * 在指定知识库下创建文档。
     *
     * @param kbId    知识库ID
     * @param request 创建文档请求体
     * @return 新创建的文档响应
     */
    @PostMapping("/{kbId}/documents")
    @ResponseStatus(HttpStatus.CREATED)
    public KbDocumentResponse createDocument(@PathVariable Long kbId, @Valid @RequestBody CreateKbDocumentRequest request) {
        return KbDocumentResponse.from(knowledgeBaseService.createDocument(request));
    }
}
