package com.aiplatform.backend.controller;

import com.aiplatform.backend.dto.CreateProviderApiKeyRequest;
import com.aiplatform.backend.dto.ProviderApiKeyResponse;
import com.aiplatform.backend.service.ProviderApiKeyService;
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
 * 上游 API 密钥管理控制器。
 * <p>提供 {@code /api/admin/provider-keys} 下的 REST 端点，支持密钥创建和查询操作。</p>
 */
@RestController
@RequestMapping("/api/admin/provider-keys")
public class ProviderApiKeyController {

    private final ProviderApiKeyService providerApiKeyService;

    /**
     * 构造方法，注入密钥业务服务。
     *
     * @param providerApiKeyService 密钥服务
     */
    public ProviderApiKeyController(ProviderApiKeyService providerApiKeyService) {
        this.providerApiKeyService = providerApiKeyService;
    }

    /**
     * 创建新的上游 API 密钥。
     *
     * @param request 创建密钥请求参数
     * @return 新创建的密钥响应
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProviderApiKeyResponse create(@Valid @RequestBody CreateProviderApiKeyRequest request) {
        return ProviderApiKeyResponse.from(providerApiKeyService.create(request));
    }

    /**
     * 查询密钥列表。
     * <p>若指定 {@code providerId}，则仅返回该供应商下的密钥；否则返回全部密钥。</p>
     *
     * @param providerId 供应商ID（可选）
     * @return 密钥响应列表
     */
    @GetMapping
    public List<ProviderApiKeyResponse> list(@RequestParam(required = false) Long providerId) {
        if (providerId != null) {
            return providerApiKeyService.listByProviderId(providerId).stream()
                    .map(ProviderApiKeyResponse::from)
                    .toList();
        }
        return providerApiKeyService.list().stream()
                .map(ProviderApiKeyResponse::from)
                .toList();
    }

    /**
     * 根据ID查询密钥详情。
     *
     * @param id 密钥ID
     * @return 密钥响应
     */
    @GetMapping("/{id}")
    public ProviderApiKeyResponse getById(@PathVariable Long id) {
        return ProviderApiKeyResponse.from(providerApiKeyService.getByIdOrThrow(id));
    }
}
