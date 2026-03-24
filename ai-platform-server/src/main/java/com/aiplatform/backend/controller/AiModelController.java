package com.aiplatform.backend.controller;

import com.aiplatform.backend.dto.AiModelResponse;
import com.aiplatform.backend.dto.CreateAiModelRequest;
import com.aiplatform.backend.service.AiModelService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * AI 模型管理控制器（完整版）。
 */
@RestController
@RequestMapping("/api/admin/models")
public class AiModelController {

    private final AiModelService aiModelService;

    public AiModelController(AiModelService aiModelService) {
        this.aiModelService = aiModelService;
    }

    /** 创建新的 AI 模型。 */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AiModelResponse create(@Valid @RequestBody CreateAiModelRequest request) {
        return AiModelResponse.from(aiModelService.create(request));
    }

    /** 查询模型列表，支持按 providerId 过滤。 */
    @GetMapping
    public List<AiModelResponse> list(@RequestParam(required = false) Long providerId) {
        if (providerId != null) {
            return aiModelService.listByProviderId(providerId).stream().map(AiModelResponse::from).toList();
        }
        return aiModelService.list().stream().map(AiModelResponse::from).toList();
    }

    /** 根据ID查询模型详情。 */
    @GetMapping("/{id}")
    public AiModelResponse getById(@PathVariable Long id) {
        return AiModelResponse.from(aiModelService.getByIdOrThrow(id));
    }

    /** 编辑模型（含定价）。 */
    @PutMapping("/{id}")
    public AiModelResponse update(@PathVariable Long id,
                                  @RequestBody CreateAiModelRequest request) {
        return AiModelResponse.from(aiModelService.update(id, request));
    }
}
