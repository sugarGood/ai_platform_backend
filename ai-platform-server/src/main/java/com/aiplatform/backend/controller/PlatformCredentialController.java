package com.aiplatform.backend.controller;

import com.aiplatform.backend.dto.CreatePlatformCredentialRequest;
import com.aiplatform.backend.dto.CreatePlatformCredentialResponse;
import com.aiplatform.backend.dto.PlatformCredentialResponse;
import com.aiplatform.backend.service.PlatformCredentialService;
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
import java.util.Map;

/**
 * 平台凭证管理控制器。
 *
 * <p>提供凭证的创建、查询和吊销等 REST API，路径前缀为 {@code /api/credentials}。</p>
 */
@RestController
@RequestMapping("/api/credentials")
public class PlatformCredentialController {

    private final PlatformCredentialService platformCredentialService;

    /** 构造函数。 */
    public PlatformCredentialController(PlatformCredentialService platformCredentialService) {
        this.platformCredentialService = platformCredentialService;
    }

    /** 创建凭证，返回包含明文密钥的响应。 */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreatePlatformCredentialResponse create(@Valid @RequestBody CreatePlatformCredentialRequest request) {
        return platformCredentialService.create(request);
    }

    /** 按用户 ID 查询凭证列表。 */
    @GetMapping
    public List<PlatformCredentialResponse> listByUserId(@RequestParam Long userId) {
        return platformCredentialService.listByUserId(userId).stream()
                .map(PlatformCredentialResponse::from)
                .toList();
    }

    /** 吊销凭证。 */
    @PostMapping("/{id}/revoke")
    public PlatformCredentialResponse revoke(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String reason = body.get("reason");
        platformCredentialService.revoke(id, reason);
        return PlatformCredentialResponse.from(platformCredentialService.getByIdOrThrow(id));
    }
}
