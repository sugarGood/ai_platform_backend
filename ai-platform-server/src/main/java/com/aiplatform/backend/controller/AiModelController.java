package com.aiplatform.backend.controller;

import com.aiplatform.backend.dto.AiModelResponse;
import com.aiplatform.backend.dto.CreateAiModelRequest;
import com.aiplatform.backend.service.AiModelService;
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

import java.util.List;

/**
 * AI 模型管理控制器。
 * <p>提供 {@code /api/admin/models} 下的 REST 端点，支持模型创建和查询操作。</p>
 */
@RestController
@RequestMapping("/api/admin/models")
public class AiModelController {

    private final AiModelService aiModelService;

    /**
     * 构造方法，注入模型业务服务。
     *
     * @param aiModelService 模型服务
     */
    public AiModelController(AiModelService aiModelService) {
        this.aiModelService = aiModelService;
    }

    /**
     * 创建新的 AI 模型。
     *
     * @param request 创建模型请求参数
     * @return 新创建的模型响应
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AiModelResponse create(@Valid @RequestBody CreateAiModelRequest request) {
        return AiModelResponse.from(aiModelService.create(request));
    }

    /**
     * 查询模型列表。
     * <p>若指定 {@code providerId}，则仅返回该供应商下的模型；否则返回全部模型。</p>
     *
     * @param providerId 供应商ID（可选）
     * @return 模型响应列表
     */
    @GetMapping
    public List<AiModelResponse> list(@RequestParam(required = false) Long providerId) {
        if (providerId != null) {
            return aiModelService.listByProviderId(providerId).stream()
                    .map(AiModelResponse::from)
                    .toList();
        }
        return aiModelService.list().stream()
                .map(AiModelResponse::from)
                .toList();
    }

    /**
     * 根据ID查询模型详情。
     *
     * @param id 模型ID
     * @return 模型响应
     */
    @GetMapping("/{id}")
    public AiModelResponse getById(@PathVariable Long id) {
        return AiModelResponse.from(aiModelService.getByIdOrThrow(id));
    }
}
