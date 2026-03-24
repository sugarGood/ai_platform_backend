package com.aiplatform.agent.gateway.controller;

import com.aiplatform.agent.gateway.entity.PlatformCredentialRef;
import com.aiplatform.agent.gateway.mapper.PlatformCredentialRefMapper;
import com.aiplatform.agent.gateway.service.CredentialAuthService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 凭证管理控制器。
 *
 * <p>提供员工自助管理个人凭证的接口，包括切换当前工作项目。
 * 员工在个人页面切换工作项目后，网关将自动使用新项目的上下文，
 * 无需修改 Cursor / Claude Code 等 AI 工具的任何配置。</p>
 */
@RestController
@RequestMapping("/api/credentials")
public class CredentialController {

    private final PlatformCredentialRefMapper credentialMapper;
    private final CredentialAuthService credentialAuthService;

    public CredentialController(PlatformCredentialRefMapper credentialMapper,
                                CredentialAuthService credentialAuthService) {
        this.credentialMapper = credentialMapper;
        this.credentialAuthService = credentialAuthService;
    }

    /**
     * 切换凭证绑定的当前工作项目。
     *
     * <p>员工在个人页面选择工作项目后调用此接口，更新凭证的 {@code bound_project_id}。
     * 切换后，Cursor / Claude Code 等 AI 工具下次发起请求时，网关将自动使用
     * 新项目的 RAG 上下文，无需任何客户端配置变更。</p>
     *
     * <p>接口只允许操作调用方自己的凭证（通过 Authorization 校验身份），
     * 防止越权修改他人凭证。</p>
     *
     * @param authorization 请求头中的 Authorization 值（Bearer Token）
     * @param credentialId  要更新的凭证 ID
     * @param body          请求体，包含 {@code projectId} 字段（传 null 表示解绑项目）
     * @return 操作结果
     */
    @PutMapping("/{credentialId}/bound-project")
    public ResponseEntity<Map<String, String>> updateBoundProject(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long credentialId,
            @RequestBody Map<String, Long> body) {

        // 1. 校验调用方身份
        PlatformCredentialRef caller = credentialAuthService.authenticate(authorization);

        // 2. 只允许修改自己的凭证
        if (!caller.getId().equals(credentialId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "只能修改自己的凭证"));
        }

        // 3. 更新 bound_project_id
        // 使用 UpdateWrapper 确保 projectId 为 null 时也能正确清空字段
        // （MyBatis-Plus 的 updateById 默认忽略 null 值）
        Long projectId = body.get("projectId");
        credentialMapper.update(null,
                Wrappers.<PlatformCredentialRef>lambdaUpdate()
                        .set(PlatformCredentialRef::getBoundProjectId, projectId)
                        .eq(PlatformCredentialRef::getId, credentialId));

        String message = projectId != null
                ? "工作项目已切换，AI 工具下次请求即生效"
                : "已解绑工作项目";
        return ResponseEntity.ok(Map.of("message", message));
    }

}
