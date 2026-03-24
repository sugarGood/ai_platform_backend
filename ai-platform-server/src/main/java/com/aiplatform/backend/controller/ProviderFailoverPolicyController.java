package com.aiplatform.backend.controller;

import com.aiplatform.backend.dto.CreateProviderFailoverPolicyRequest;
import com.aiplatform.backend.dto.ProviderFailoverPolicyResponse;
import com.aiplatform.backend.service.ProviderFailoverPolicyService;
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
 * 故障转移策略管理控制器（完整版）。
 */
@RestController
@RequestMapping("/api/admin/failover-policies")
public class ProviderFailoverPolicyController {

    private final ProviderFailoverPolicyService providerFailoverPolicyService;

    public ProviderFailoverPolicyController(ProviderFailoverPolicyService providerFailoverPolicyService) {
        this.providerFailoverPolicyService = providerFailoverPolicyService;
    }

    /** 创建故障转移策略。 */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProviderFailoverPolicyResponse create(
            @Valid @RequestBody CreateProviderFailoverPolicyRequest request) {
        return ProviderFailoverPolicyResponse.from(providerFailoverPolicyService.create(request));
    }

    /** 查询全部故障转移策略列表。 */
    @GetMapping
    public List<ProviderFailoverPolicyResponse> list() {
        return providerFailoverPolicyService.list().stream()
                .map(ProviderFailoverPolicyResponse::from).toList();
    }

    /** 根据ID查询策略详情。 */
    @GetMapping("/{id}")
    public ProviderFailoverPolicyResponse getById(@PathVariable Long id) {
        return ProviderFailoverPolicyResponse.from(providerFailoverPolicyService.getByIdOrThrow(id));
    }

    /** 编辑故障转移策略。 */
    @PutMapping("/{id}")
    public ProviderFailoverPolicyResponse update(
            @PathVariable Long id,
            @RequestBody CreateProviderFailoverPolicyRequest request) {
        return ProviderFailoverPolicyResponse.from(providerFailoverPolicyService.update(id, request));
    }
}
