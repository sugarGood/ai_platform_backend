package com.aiplatform.backend.controller;

import com.aiplatform.backend.dto.ChangePasswordRequest;
import com.aiplatform.backend.dto.LoginRequest;
import com.aiplatform.backend.dto.LoginResponse;
import com.aiplatform.backend.dto.RefreshTokenRequest;
import com.aiplatform.backend.dto.UserResponse;
import com.aiplatform.backend.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 认证控制器。
 *
 * <p>提供 {@code /api/auth} 下的登录、登出、Token 刷新及当前用户查询端点。</p>
 *
 * <pre>
 * POST /api/auth/login          - 邮箱 + 密码登录，返回 Access/Refresh Token
 * POST /api/auth/refresh         - 用 Refresh Token 换取新 Access Token
 * POST /api/auth/logout          - 客户端主动登出（服务端无状态，返回提示即可）
 * GET  /api/auth/me              - 获取当前登录用户信息
 * POST /api/auth/change-password - 修改当前用户密码
 * </pre>
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * 用户登录。
     *
     * <p>校验邮箱和密码后返回 Access Token（默认 24h）和 Refresh Token（默认 7d）。
     * Access Token 在后续请求中以 {@code Authorization: Bearer <token>} 方式携带。</p>
     *
     * @param request 登录请求（email + password）
     * @return 登录响应（含双 Token 及用户信息）
     */
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    /**
     * 刷新 Access Token。
     *
     * <p>使用有效的 Refresh Token 换取新的 Access Token，Refresh Token 本身不轮换。</p>
     *
     * @param request 包含 refreshToken 字段的请求体
     * @return 新的登录响应（含新 Access Token）
     */
    @PostMapping("/refresh")
    public LoginResponse refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return authService.refresh(request.refreshToken());
    }

    /**
     * 获取当前登录用户信息。
     *
     * <p>从请求头 {@code Authorization: Bearer <token>} 中解析用户身份。</p>
     *
     * @param authorization Authorization 请求头
     * @return 当前用户信息
     */
    @GetMapping("/me")
    public UserResponse me(@RequestHeader("Authorization") String authorization) {
        return authService.getCurrentUser(authorization);
    }

    /**
     * 修改当前用户密码。
     *
     * @param authorization Authorization 请求头（Bearer Token）
     * @param request       包含 oldPassword 和 newPassword 的请求体
     * @return 操作成功消息
     */
    @PostMapping("/change-password")
    public ResponseEntity<Map<String, String>> changePassword(
            @RequestHeader("Authorization") String authorization,
            @Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(authorization, request);
        return ResponseEntity.ok(Map.of("message", "密码修改成功"));
    }

    /**
     * 用户登出。
     *
     * <p>服务端采用无状态 JWT，登出仅在客户端丢弃 Token 即可。
     * 此端点返回提示信息，配合前端清理本地存储。</p>
     *
     * @return 登出成功消息
     */
    @PostMapping("/logout")
    public Map<String, String> logout() {
        return Map.of("message", "已退出登录，请清除本地 Token");
    }
}
