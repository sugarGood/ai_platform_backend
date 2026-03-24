package com.aiplatform.backend.controller;

import com.aiplatform.backend.dto.CreateProviderApiKeyRequest;
import com.aiplatform.backend.dto.ProviderApiKeyResponse;
import com.aiplatform.backend.service.ProviderApiKeyService;
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
import java.util.Map;

/**
 * 上游 API 密钥管理控制器（完整版）。
 */
@RestController
@RequestMapping("/api/admin/provider-keys")
public class ProviderApiKeyController {

    private final ProviderApiKeyService providerApiKeyService;

    public ProviderApiKeyController(ProviderApiKeyService providerApiKeyService) {
        this.providerApiKeyService = providerApiKeyService;
    }

    /** 创建新的上游 API 密钥。 */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProviderApiKeyResponse create(@Valid @RequestBody CreateProviderApiKeyRequest request) {
        return ProviderApiKeyResponse.from(providerApiKeyService.create(request));
    }

    /** 查询密钥列表，支持按 providerId 过滤。 */
    @GetMapping
    public List<ProviderApiKeyResponse> list(@RequestParam(required = false) Long providerId) {
        if (providerId != null) {
            return providerApiKeyService.listByProviderId(providerId).stream()
                    .map(ProviderApiKeyResponse::from).toList();
        }
        return providerApiKeyService.list().stream().map(ProviderApiKeyResponse::from).toList();
    }

    /** 根据ID查询密钥详情。 */
    @GetMapping("/{id}")
    public ProviderApiKeyResponse getById(@PathVariable Long id) {
        return ProviderApiKeyResponse.from(providerApiKeyService.getByIdOrThrow(id));
    }

    /** 编辑Key配置（配额、限速等）。 */
    @PutMapping("/{id}")
    public ProviderApiKeyResponse update(@PathVariable Long id,
                                         @RequestBody CreateProviderApiKeyRequest request) {
        return ProviderApiKeyResponse.from(providerApiKeyService.update(id, request));
    }

    /** 吊销 Key。 */
    @PostMapping("/{id}/revoke")
    public ProviderApiKeyResponse revoke(@PathVariable Long id) {
        return ProviderApiKeyResponse.from(providerApiKeyService.revoke(id));
    }

    /**
     * 测试 Key 连通性。
     * TODO: 接入实际 API 调用验证。
     */
    @PostMapping("/{id}/test")
    public Map<String, Object> test(@PathVariable Long id) {
        providerApiKeyService.getByIdOrThrow(id);
        return Map.of(
                "keyId", id,
                "status", "pending",
                "message", "连通性测试引擎待集成"
        );
    }
}
