package com.aiplatform.backend.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.aiplatform.backend.dto.ChangePasswordRequest;
import com.aiplatform.backend.dto.LoginRequest;
import com.aiplatform.backend.dto.LoginResponse;
import com.aiplatform.backend.dto.RefreshTokenRequest;
import com.aiplatform.backend.dto.SimpleMessageResponse;
import com.aiplatform.backend.dto.UserResponse;
import com.aiplatform.backend.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/refresh")
    public LoginResponse refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return authService.refresh(request.refreshToken());
    }

    @GetMapping("/me")
    public UserResponse me() {
        return authService.getCurrentUser();
    }

    @PostMapping("/change-password")
    public ResponseEntity<SimpleMessageResponse> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(request);
        return ResponseEntity.ok(new SimpleMessageResponse("Password changed successfully"));
    }

    @PostMapping("/logout")
    public SimpleMessageResponse logout() {
        if (StpUtil.isLogin()) {
            StpUtil.logout();
        }
        return new SimpleMessageResponse("Logged out");
    }
}
