package com.aiplatform.backend.controller;

import com.aiplatform.backend.dto.AiProviderResponse;
import com.aiplatform.backend.dto.CreateAiProviderRequest;
import com.aiplatform.backend.service.AiProviderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * AI 供应商管理控制器（完整版）。
 */
@RestController
@RequestMapping("/api/admin/providers")
public class AiProviderController {

    private final AiProviderService aiProviderService;

    public AiProviderController(AiProviderService aiProviderService) {
        this.aiProviderService = aiProviderService;
    }

    /** 创建新的 AI 供应商。 */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AiProviderResponse create(@Valid @RequestBody CreateAiProviderRequest request) {
        return AiProviderResponse.from(aiProviderService.create(request));
    }

    /** 查询全部供应商列表。 */
    @GetMapping
    public List<AiProviderResponse> list() {
        return aiProviderService.list().stream().map(AiProviderResponse::from).toList();
    }

    /** 根据ID查询供应商详情。 */
    @GetMapping("/{id}")
    public AiProviderResponse getById(@PathVariable Long id) {
        return AiProviderResponse.from(aiProviderService.getByIdOrThrow(id));
    }

    /** 编辑供应商信息。 */
    @PutMapping("/{id}")
    public AiProviderResponse update(@PathVariable Long id,
                                     @RequestBody CreateAiProviderRequest request) {
        return AiProviderResponse.from(aiProviderService.update(id, request));
    }
}
