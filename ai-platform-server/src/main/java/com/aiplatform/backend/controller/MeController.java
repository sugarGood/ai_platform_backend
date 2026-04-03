package com.aiplatform.backend.controller;

import com.aiplatform.backend.common.exception.UnauthorizedException;
import com.aiplatform.backend.common.security.AuthContext;
import com.aiplatform.backend.dto.CreatePlatformCredentialResponse;
import com.aiplatform.backend.dto.PlatformCredentialResponse;
import com.aiplatform.backend.dto.me.MeCredentialRenewRequest;
import com.aiplatform.backend.dto.me.MeCredentialResponse;
import com.aiplatform.backend.dto.me.MeCredentialRotateRequest;
import com.aiplatform.backend.dto.me.MeCredentialTestRequest;
import com.aiplatform.backend.dto.me.MeCredentialTestResponse;
import com.aiplatform.backend.dto.me.MeCurrentProjectRequest;
import com.aiplatform.backend.dto.me.MeSetupMaterialItemResponse;
import com.aiplatform.backend.dto.me.MeSetupMaterialsResponse;
import com.aiplatform.backend.entity.ClientApp;
import com.aiplatform.backend.entity.PlatformCredential;
import com.aiplatform.backend.entity.UserClientBinding;
import com.aiplatform.backend.dto.ProjectMemberPermissionOverridesResponse;
import com.aiplatform.backend.service.ClientAppService;
import com.aiplatform.backend.service.PlatformCredentialService;
import com.aiplatform.backend.service.ProjectMemberRbacService;
import com.aiplatform.backend.service.UserClientBindingService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 个人工作台凭证聚合接口。
 */
@RestController
@RequestMapping("/api/me")
public class MeController {

    private static final int DEFAULT_RENEW_DAYS = 90;
    private static final int DEFAULT_GRACE_PERIOD_HOURS = 24;

    private final PlatformCredentialService platformCredentialService;
    private final ClientAppService clientAppService;
    private final UserClientBindingService userClientBindingService;
    private final ProjectMemberRbacService projectMemberRbacService;

    public MeController(PlatformCredentialService platformCredentialService,
                        ClientAppService clientAppService,
                        UserClientBindingService userClientBindingService,
                        ProjectMemberRbacService projectMemberRbacService) {
        this.platformCredentialService = platformCredentialService;
        this.clientAppService = clientAppService;
        this.userClientBindingService = userClientBindingService;
        this.projectMemberRbacService = projectMemberRbacService;
    }

    /**
     * 获取当前成员的个人凭证信息。
     */
    @GetMapping("/credential")
    public MeCredentialResponse credential() {
        return platformCredentialService.getMeCredential(currentUserId());
    }

    /**
     * 获取按客户端聚合的接入材料。
     */
    @GetMapping("/setup-materials")
    public MeSetupMaterialsResponse setupMaterials() {
        Long userId = currentUserId();
        List<ClientApp> apps = clientAppService.list();
        Map<Long, UserClientBinding> bindingMap = userClientBindingService.listByUserId(userId).stream()
                .collect(Collectors.toMap(UserClientBinding::getClientAppId, Function.identity(), (a, b) -> a));

        List<MeSetupMaterialItemResponse> materials = apps.stream()
                .map(app -> {
                    UserClientBinding binding = bindingMap.get(app.getId());
                    return new MeSetupMaterialItemResponse(
                            app.getId(),
                            app.getCode(),
                            app.getName(),
                            app.getIcon(),
                            app.getSupportsMcp(),
                            app.getSupportsCustomGateway(),
                            app.getSetupInstruction(),
                            binding == null ? null : binding.getBindingStatus(),
                            binding == null ? null : binding.getLastActiveAt());
                })
                .toList();
        return new MeSetupMaterialsResponse(materials);
    }

    /**
     * 轮换当前用户个人凭证。
     */
    @PostMapping("/credential/rotate")
    public CreatePlatformCredentialResponse rotate(@Valid @RequestBody(required = false) MeCredentialRotateRequest request) {
        int gracePeriodHours = request != null && request.gracePeriodHours() != null
                ? request.gracePeriodHours()
                : DEFAULT_GRACE_PERIOD_HOURS;
        return platformCredentialService.rotateByUserId(currentUserId(), gracePeriodHours);
    }

    /**
     * 续签当前用户个人凭证。
     */
    @PostMapping("/credential/renew")
    public PlatformCredentialResponse renew(@Valid @RequestBody(required = false) MeCredentialRenewRequest request) {
        int renewDays = request != null && request.renewDays() != null
                ? request.renewDays()
                : DEFAULT_RENEW_DAYS;
        return PlatformCredentialResponse.from(platformCredentialService.renewByUserId(currentUserId(), renewDays));
    }

    /**
     * 测试当前用户凭证连通性。
     */
    @PostMapping("/credential/test")
    public MeCredentialTestResponse test(@RequestBody(required = false) MeCredentialTestRequest request) {
        Long userId = currentUserId();
        Long clientAppId = request == null ? null : request.clientAppId();
        userClientBindingService.touchLastActiveAt(userId, clientAppId);
        platformCredentialService.recordCredentialTestActivity(userId, clientAppId);
        Long credentialId = platformCredentialService.getByUserIdOrThrow(userId).getId();
        return new MeCredentialTestResponse(true, "凭证连通性测试通过", credentialId);
    }

    /**
     * 获取当前成员在项目中的 AI 能力收敛结果。
     */
    @GetMapping("/abilities")
    public ProjectMemberPermissionOverridesResponse abilities(
            @org.springframework.web.bind.annotation.RequestParam(required = false) Long projectId) {
        Long userId = currentUserId();
        Long resolvedProjectId = projectId;
        if (resolvedProjectId == null) {
            PlatformCredential credential = platformCredentialService.getByUserIdOrThrow(userId);
            resolvedProjectId = credential.getBoundProjectId();
            if (resolvedProjectId == null) {
                throw new UnauthorizedException("未设置当前工作项目");
            }
        }
        return projectMemberRbacService.getPermissionOverridesByUser(resolvedProjectId, userId);
    }

    /**
     * 切换当前工作项目。
     */
    @PutMapping("/current-project")
    public PlatformCredentialResponse updateCurrentProject(@RequestBody(required = false) MeCurrentProjectRequest request) {
        Long projectId = request == null ? null : request.projectId();
        return PlatformCredentialResponse.from(
                platformCredentialService.updateCurrentProjectByUserId(currentUserId(), projectId)
        );
    }

    private Long currentUserId() {
        Long userId = AuthContext.getUserId();
        if (userId == null) {
            throw new UnauthorizedException("未登录或登录已失效");
        }
        return userId;
    }
}
