package com.aiplatform.backend.controller;

import com.aiplatform.backend.common.security.AuthContext;
import com.aiplatform.backend.dto.CreatePlatformCredentialRequest;
import com.aiplatform.backend.dto.CreatePlatformCredentialResponse;
import com.aiplatform.backend.dto.KeyRotationLogResponse;
import com.aiplatform.backend.dto.PlatformCredentialResponse;
import com.aiplatform.backend.service.ClientAppService;
import com.aiplatform.backend.service.PlatformCredentialService;
import com.aiplatform.backend.service.UserClientBindingService;
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
 * 平台凭证管理控制器。
 *
 * <p>提供凭证的创建、查询、续期、轮换、吊销、工作项目绑定等 REST API，路径前缀为 {@code /api/credentials}。</p>
 */
@RestController
@RequestMapping("/api/credentials")
public class PlatformCredentialController {

    private final PlatformCredentialService platformCredentialService;
    private final ClientAppService clientAppService;
    private final UserClientBindingService userClientBindingService;

    public PlatformCredentialController(PlatformCredentialService platformCredentialService,
                                        ClientAppService clientAppService,
                                        UserClientBindingService userClientBindingService) {
        this.platformCredentialService = platformCredentialService;
        this.clientAppService = clientAppService;
        this.userClientBindingService = userClientBindingService;
    }

    /** 创建凭证，返回包含明文密钥的响应（明文仅此一次）。 */
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

    /** 根据凭证 ID 查询单条凭证详情。 */
    @GetMapping("/{id}")
    public PlatformCredentialResponse getById(@PathVariable Long id) {
        return PlatformCredentialResponse.from(platformCredentialService.getByIdOrThrow(id));
    }

    /** 吊销凭证。 */
    @PostMapping("/{id}/revoke")
    public PlatformCredentialResponse revoke(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String reason = body.get("reason");
        platformCredentialService.revoke(id, reason);
        return PlatformCredentialResponse.from(platformCredentialService.getByIdOrThrow(id));
    }

    /**
     * 续期凭证（延长过期时间）。
     *
     * @param id   凭证ID
     * @param body 请求体，包含 {@code renewDays} 字段（30/90/180）
     */
    @PostMapping("/{id}/renew")
    public PlatformCredentialResponse renew(@PathVariable Long id,
                                            @RequestBody Map<String, Object> body) {
        int renewDays = body.containsKey("renewDays") ? ((Number) body.get("renewDays")).intValue() : 90;
        return PlatformCredentialResponse.from(platformCredentialService.renew(id, renewDays));
    }

    /**
     * 轮换凭证（生成新密钥，旧密钥有宽限期）。
     *
     * @param id   凭证ID
     * @param body 请求体，包含 {@code gracePeriodHours} 字段（默认24）
     * @return 含新明文密钥的响应（明文仅此一次）
     */
    @PostMapping("/{id}/rotate")
    public CreatePlatformCredentialResponse rotate(@PathVariable Long id,
                                                   @RequestBody(required = false) Map<String, Object> body) {
        int gracePeriodHours = (body != null && body.containsKey("gracePeriodHours"))
                ? ((Number) body.get("gracePeriodHours")).intValue() : 24;
        return platformCredentialService.rotate(id, gracePeriodHours);
    }

    /**
     * 切换当前工作项目，写入 {@code bound_project_id}；AI 网关只读该字段（与 {@code X-Project-Id} 优先级见网关文档）。
     * 请求体含 {@code projectId}，可为 {@code null} 表示解绑。
     */
    @PutMapping("/{id}/bound-project")
    public PlatformCredentialResponse updateBoundProject(@PathVariable Long id,
                                                         @RequestBody(required = false) Map<String, Long> body) {
        Long projectId = body != null ? body.get("projectId") : null;
        return PlatformCredentialResponse.from(
                platformCredentialService.updateBoundProject(id, AuthContext.getUserId(), projectId));
    }

    /** 查询凭证轮换日志。 */
    @GetMapping("/{id}/rotation-logs")
    public List<KeyRotationLogResponse> rotationLogs(@PathVariable Long id) {
        return platformCredentialService.listRotationLogs(id);
    }

    /** 管理员视图：查询全平台凭证列表，支持按状态和用户过滤。 */
    @GetMapping("/admin")
    public List<PlatformCredentialResponse> adminList(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long userId) {
        return platformCredentialService.adminList(status, userId)
                .stream().map(PlatformCredentialResponse::from).toList();
    }

    /** 查询客户端应用列表（Claude Code / Cursor 等）。 */
    @GetMapping("/client-apps")
    public Object listClientApps() {
        return clientAppService.list().stream()
                .map(com.aiplatform.backend.dto.ClientAppResponse::from).toList();
    }

    /** 查询指定用户的客户端绑定列表。 */
    @GetMapping("/client-bindings")
    public Object listClientBindings(@RequestParam Long userId) {
        return userClientBindingService.listByUserId(userId).stream()
                .map(com.aiplatform.backend.dto.UserClientBindingResponse::from).toList();
    }

    /** 创建用户客户端绑定。 */
    @PostMapping("/client-bindings")
    @ResponseStatus(HttpStatus.CREATED)
    public Object createClientBinding(
            @RequestBody com.aiplatform.backend.dto.CreateUserClientBindingRequest request) {
        return com.aiplatform.backend.dto.UserClientBindingResponse.from(
                userClientBindingService.create(request));
    }
}
