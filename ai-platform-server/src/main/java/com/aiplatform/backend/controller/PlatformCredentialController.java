package com.aiplatform.backend.controller;

import com.aiplatform.backend.common.security.AuthContext;
import com.aiplatform.backend.dto.ClientAppResponse;
import com.aiplatform.backend.dto.CreatePlatformCredentialRequest;
import com.aiplatform.backend.dto.CreatePlatformCredentialResponse;
import com.aiplatform.backend.dto.CreateUserClientBindingRequest;
import com.aiplatform.backend.dto.CredentialBoundProjectRequest;
import com.aiplatform.backend.dto.CredentialRenewRequest;
import com.aiplatform.backend.dto.CredentialRevokeRequest;
import com.aiplatform.backend.dto.CredentialRotateRequest;
import com.aiplatform.backend.dto.KeyRotationLogResponse;
import com.aiplatform.backend.dto.PlatformCredentialResponse;
import com.aiplatform.backend.dto.UserClientBindingResponse;
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

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreatePlatformCredentialResponse create(@Valid @RequestBody CreatePlatformCredentialRequest request) {
        return platformCredentialService.create(request);
    }

    @GetMapping
    public List<PlatformCredentialResponse> listByUserId(@RequestParam Long userId) {
        return platformCredentialService.listByUserId(userId).stream()
                .map(PlatformCredentialResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public PlatformCredentialResponse getById(@PathVariable Long id) {
        return PlatformCredentialResponse.from(platformCredentialService.getByIdOrThrow(id));
    }

    @PostMapping("/{id}/revoke")
    public PlatformCredentialResponse revoke(@PathVariable Long id,
                                             @RequestBody(required = false) CredentialRevokeRequest request) {
        platformCredentialService.revoke(id, request != null ? request.reason() : null);
        return PlatformCredentialResponse.from(platformCredentialService.getByIdOrThrow(id));
    }

    @PostMapping("/{id}/renew")
    public PlatformCredentialResponse renew(@PathVariable Long id,
                                            @Valid @RequestBody CredentialRenewRequest request) {
        int renewDays = request.renewDays() != null ? request.renewDays() : 90;
        return PlatformCredentialResponse.from(platformCredentialService.renew(id, renewDays));
    }

    @PostMapping("/{id}/rotate")
    public CreatePlatformCredentialResponse rotate(@PathVariable Long id,
                                                   @RequestBody(required = false) @Valid CredentialRotateRequest request) {
        int gracePeriodHours = request != null && request.gracePeriodHours() != null
                ? request.gracePeriodHours()
                : 24;
        return platformCredentialService.rotate(id, gracePeriodHours);
    }

    @PutMapping("/{id}/bound-project")
    public PlatformCredentialResponse updateBoundProject(@PathVariable Long id,
                                                         @RequestBody(required = false) CredentialBoundProjectRequest request) {
        Long projectId = request != null ? request.projectId() : null;
        return PlatformCredentialResponse.from(
                platformCredentialService.updateBoundProject(id, AuthContext.getUserId(), projectId)
        );
    }

    @GetMapping("/{id}/rotation-logs")
    public List<KeyRotationLogResponse> rotationLogs(@PathVariable Long id) {
        return platformCredentialService.listRotationLogs(id);
    }

    @GetMapping("/admin")
    public List<PlatformCredentialResponse> adminList(@RequestParam(required = false) String status,
                                                      @RequestParam(required = false) Long userId) {
        return platformCredentialService.adminList(status, userId).stream()
                .map(PlatformCredentialResponse::from)
                .toList();
    }

    @GetMapping("/client-apps")
    public List<ClientAppResponse> listClientApps() {
        return clientAppService.list().stream()
                .map(ClientAppResponse::from)
                .toList();
    }

    @GetMapping("/client-bindings")
    public List<UserClientBindingResponse> listClientBindings(@RequestParam Long userId) {
        return userClientBindingService.listByUserId(userId).stream()
                .map(UserClientBindingResponse::from)
                .toList();
    }

    @PostMapping("/client-bindings")
    @ResponseStatus(HttpStatus.CREATED)
    public UserClientBindingResponse createClientBinding(@RequestBody CreateUserClientBindingRequest request) {
        return UserClientBindingResponse.from(userClientBindingService.create(request));
    }
}
