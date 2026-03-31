package com.aiplatform.backend.controller;

import com.aiplatform.backend.dto.ChangePasswordRequest;
import com.aiplatform.backend.dto.LoginRequest;
import com.aiplatform.backend.dto.LoginResponse;
import cn.dev33.satoken.stp.StpUtil;
import com.aiplatform.backend.dto.RefreshTokenRequest;
import com.aiplatform.backend.dto.UserResponse;
import com.aiplatform.backend.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 认证控制器。
 *
 * <p>提供 {@code /api/auth} 下的登录、登出、Token 刷新及当前用户查询端点；登录态由 Sa-Token 管理。</p>
 *
 * <pre>
 * POST /api/auth/login          - 邮箱 + 密码登录，返回 Token（access 与 refresh 当前为同值）
 * POST /api/auth/refresh         - 用 Refresh Token 轮换会话并取新 Token
 * POST /api/auth/logout          - 服务端注销 Sa-Token 会话
 * GET  /api/auth/me              - 获取当前登录用户信息（需 Authorization: Bearer）
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
     * <p>校验邮箱和密码后创建 Sa-Token 会话；响应中 accessToken 与 refreshToken 当前为同一会话 Token，
     * 后续请求以 {@code Authorization: Bearer <token>} 携带。</p>
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
    public UserResponse me() {
        return authService.getCurrentUser();
    }

    /**
     * 修改当前用户密码。
     *
     * @param authorization Authorization 请求头（Bearer Token）
     * @param request       包含 oldPassword 和 newPassword 的请求体
     * @return 操作成功消息
     */
    @PostMapping("/change-password")
    public ResponseEntity<Map<String, String>> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(request);
        return ResponseEntity.ok(Map.of("message", "密码修改成功"));
    }

    /**
     * 用户登出，注销当前 Sa-Token 会话。
     */
    @PostMapping("/logout")
    public Map<String, String> logout() {
        if (StpUtil.isLogin()) {
            StpUtil.logout();
        }
        return Map.of("message", "已退出登录");
    }
}
