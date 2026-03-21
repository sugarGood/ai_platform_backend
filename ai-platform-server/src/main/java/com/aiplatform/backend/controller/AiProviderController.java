package com.aiplatform.backend.controller;

import com.aiplatform.backend.dto.AiProviderResponse;
import com.aiplatform.backend.dto.CreateAiProviderRequest;
import com.aiplatform.backend.service.AiProviderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * AI 供应商管理控制器。
 * <p>提供 {@code /api/admin/providers} 下的 REST 端点，支持供应商创建和查询操作。</p>
 */
@RestController
@RequestMapping("/api/admin/providers")
public class AiProviderController {

    private final AiProviderService aiProviderService;

    /**
     * 构造方法，注入供应商业务服务。
     *
     * @param aiProviderService 供应商服务
     */
    public AiProviderController(AiProviderService aiProviderService) {
        this.aiProviderService = aiProviderService;
    }

    /**
     * 创建新的 AI 供应商。
     *
     * @param request 创建供应商请求参数
     * @return 新创建的供应商响应
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AiProviderResponse create(@Valid @RequestBody CreateAiProviderRequest request) {
        return AiProviderResponse.from(aiProviderService.create(request));
    }

    /**
     * 查询全部供应商列表。
     *
     * @return 供应商响应列表
     */
    @GetMapping
    public List<AiProviderResponse> list() {
        return aiProviderService.list().stream()
                .map(AiProviderResponse::from)
                .toList();
    }

    /**
     * 根据ID查询供应商详情。
     *
     * @param id 供应商ID
     * @return 供应商响应
     */
    @GetMapping("/{id}")
    public AiProviderResponse getById(@PathVariable Long id) {
        return AiProviderResponse.from(aiProviderService.getByIdOrThrow(id));
    }
}
