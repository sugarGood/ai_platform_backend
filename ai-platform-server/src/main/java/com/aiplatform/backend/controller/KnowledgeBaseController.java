package com.aiplatform.backend.controller;

import com.aiplatform.backend.dto.CreateKnowledgeBaseRequest;
import com.aiplatform.backend.dto.KbDocumentResponse;
import com.aiplatform.backend.dto.KnowledgeBaseResponse;
import com.aiplatform.backend.dto.KnowledgeBaseSearchRequest;
import com.aiplatform.backend.dto.KnowledgeDashboardResponse;
import com.aiplatform.backend.dto.UpdateKnowledgeBaseRagConfigRequest;
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

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public KnowledgeBaseResponse create(@Valid @RequestBody CreateKnowledgeBaseRequest request) {
        return KnowledgeBaseResponse.from(knowledgeBaseService.create(request));
    }

    @GetMapping
    public List<KnowledgeBaseResponse> list(@RequestParam(required = false) String scope) {
        if (scope != null) {
            return knowledgeBaseService.listByScope(scope).stream().map(KnowledgeBaseResponse::from).toList();
        }
        return knowledgeBaseService.list().stream().map(KnowledgeBaseResponse::from).toList();
    }

    @GetMapping("/dashboard")
    public KnowledgeDashboardResponse dashboard() {
        return knowledgeDashboardService.getDashboard();
    }

    @GetMapping("/{id}")
    public KnowledgeBaseResponse getById(@PathVariable Long id) {
        return KnowledgeBaseResponse.from(knowledgeBaseService.getByIdOrThrow(id));
    }

    @PutMapping("/{id}")
    public KnowledgeBaseResponse update(@PathVariable Long id,
                                        @RequestBody CreateKnowledgeBaseRequest request) {
        return KnowledgeBaseResponse.from(knowledgeBaseService.update(id, request));
    }

    @PostMapping("/{id}/archive")
    public KnowledgeBaseResponse archive(@PathVariable Long id) {
        return KnowledgeBaseResponse.from(knowledgeBaseService.archive(id));
    }

    @GetMapping("/{kbId}/documents")
    public List<KbDocumentResponse> listDocuments(@PathVariable Long kbId) {
        return knowledgeBaseService.listDocuments(kbId).stream().map(KbDocumentResponse::from).toList();
    }

    @PostMapping(value = "/{kbId}/documents/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public KbDocumentResponse uploadDocument(@PathVariable Long kbId,
                                             @RequestPart("file") MultipartFile file,
                                             @RequestParam(required = false) String title,
                                             @RequestParam(required = false) String injectMode) {
        return KbDocumentResponse.from(knowledgeBaseService.uploadDocument(kbId, file, title, injectMode));
    }

    /**
     * 兼容文档接口路径：与原型约定的 /documents 保持一致。
     */
    @PostMapping(value = "/{kbId}/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public KbDocumentResponse uploadDocumentCompat(@PathVariable Long kbId,
                                                   @RequestPart("file") MultipartFile file,
                                                   @RequestParam(required = false) String title,
                                                   @RequestParam(required = false) String injectMode) {
        return KbDocumentResponse.from(knowledgeBaseService.uploadDocument(kbId, file, title, injectMode));
    }

    @PostMapping("/{kbId}/documents/{docId}/reingest")
    public KbDocumentResponse reingestDocument(@PathVariable Long kbId, @PathVariable Long docId) {
        return KbDocumentResponse.from(knowledgeBaseService.reingestDocument(kbId, docId));
    }

    @GetMapping("/{kbId}/documents/{docId}")
    public KbDocumentResponse getDocument(@PathVariable Long kbId, @PathVariable Long docId) {
        return KbDocumentResponse.from(knowledgeBaseService.getDocumentByIdOrThrow(docId));
    }

    @DeleteMapping("/{kbId}/documents/{docId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDocument(@PathVariable Long kbId, @PathVariable Long docId) {
        knowledgeBaseService.deleteDocument(kbId, docId);
    }

    @PostMapping("/{kbId}/search")
    public Map<String, Object> search(@PathVariable Long kbId,
                                      @Valid @RequestBody KnowledgeBaseSearchRequest request) {
        return knowledgeBaseService.searchKnowledgeBase(kbId, request);
    }

    /**
     * 兼容检索测试路径：/search-test。
     */
    @PostMapping("/{kbId}/search-test")
    public Map<String, Object> searchTest(@PathVariable Long kbId,
                                          @Valid @RequestBody KnowledgeBaseSearchRequest request) {
        return knowledgeBaseService.searchKnowledgeBase(kbId, request);
    }

    @GetMapping("/{kbId}/rag-config")
    public KnowledgeBaseResponse getRagConfig(@PathVariable Long kbId) {
        return KnowledgeBaseResponse.from(knowledgeBaseService.getByIdOrThrow(kbId));
    }

    @PutMapping("/{kbId}/rag-config")
    public KnowledgeBaseResponse updateRagConfig(@PathVariable Long kbId,
                                                 @RequestBody UpdateKnowledgeBaseRagConfigRequest request) {
        return KnowledgeBaseResponse.from(knowledgeBaseService.updateRagConfig(kbId, request));
    }
}
