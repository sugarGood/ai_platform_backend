package com.aiplatform.backend.service;

import com.aiplatform.backend.common.exception.UnauthorizedException;
import com.aiplatform.backend.common.exception.UserNotFoundException;
import com.aiplatform.backend.common.util.JwtUtils;
import com.aiplatform.backend.dto.ChangePasswordRequest;
import com.aiplatform.backend.dto.LoginRequest;
import com.aiplatform.backend.dto.LoginResponse;
import com.aiplatform.backend.dto.UserResponse;
import com.aiplatform.backend.entity.User;
import com.aiplatform.backend.mapper.UserMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 认证业务服务。
 *
 * <p>提供登录、Token 刷新、登出、获取当前用户信息及修改密码等功能。
 * 密码采用 BCrypt 单向哈希存储，不可逆。</p>
 */
@Service
public class AuthService {

    private final UserMapper userMapper;
    private final JwtUtils jwtUtils;
    private final BCryptPasswordEncoder passwordEncoder;

    @Value("${jwt.access-token-ttl-seconds:86400}")
    private long accessTokenTtlSeconds;

    public AuthService(UserMapper userMapper, JwtUtils jwtUtils) {
        this.userMapper = userMapper;
        this.jwtUtils = jwtUtils;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    /**
     * 用户登录，校验邮箱和密码后颁发 Access Token 与 Refresh Token。
     *
     * @param request 登录请求（邮箱 + 明文密码）
     * @return 登录响应（含双 Token 及用户信息）
     * @throws UnauthorizedException 邮箱不存在、密码错误或账号被禁用时抛出
     */
    public LoginResponse login(LoginRequest request) {
        // 1. 查用户
        User user = userMapper.selectOne(
                Wrappers.<User>lambdaQuery().eq(User::getEmail, request.email()));
        if (user == null) {
            throw new UnauthorizedException("邮箱或密码不正确");
        }

        // 2. 校验密码
        if (user.getPasswordHash() == null
                || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new UnauthorizedException("邮箱或密码不正确");
        }

        // 3. 校验账号状态
        if ("DISABLED".equals(user.getStatus())) {
            throw new UnauthorizedException("账号已被禁用，请联系管理员");
        }
        if ("INACTIVE".equals(user.getStatus())) {
            throw new UnauthorizedException("账号尚未激活，请先完成邮件验证");
        }

        // 4. 颁发 Token
        String accessToken  = jwtUtils.generateAccessToken(user.getId(), user.getEmail(), user.getPlatformRole());
        String refreshToken = jwtUtils.generateRefreshToken(user.getId(), user.getEmail());

        return LoginResponse.of(accessToken, refreshToken, accessTokenTtlSeconds, UserResponse.from(user));
    }

    /**
     * 使用 Refresh Token 续签，颁发新的 Access Token。
     *
     * @param refreshToken 有效的 Refresh Token
     * @return 新的登录响应（含新 Access Token，Refresh Token 不轮换）
     * @throws UnauthorizedException Refresh Token 无效或用户不存在时抛出
     */
    public LoginResponse refresh(String refreshToken) {
        Claims claims;
        try {
            claims = jwtUtils.parseToken(refreshToken);
        } catch (JwtException e) {
            throw new UnauthorizedException("Refresh Token 无效或已过期");
        }

        Long userId = claims.get("uid", Long.class);
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new UnauthorizedException("用户不存在");
        }
        if ("DISABLED".equals(user.getStatus())) {
            throw new UnauthorizedException("账号已被禁用");
        }

        String newAccessToken = jwtUtils.generateAccessToken(user.getId(), user.getEmail(), user.getPlatformRole());
        return LoginResponse.of(newAccessToken, refreshToken, accessTokenTtlSeconds, UserResponse.from(user));
    }

    /**
     * 根据 Authorization 请求头中的 Bearer Token 解析当前登录用户。
     *
     * @param authorizationHeader HTTP {@code Authorization} 头的值（{@code Bearer <token>}）
     * @return 当前用户响应 DTO
     * @throws UnauthorizedException Token 缺失、无效或用户不存在时抛出
     */
    public UserResponse getCurrentUser(String authorizationHeader) {
        String token = extractBearerToken(authorizationHeader);
        Long userId;
        try {
            userId = jwtUtils.extractUserId(token);
        } catch (JwtException e) {
            throw new UnauthorizedException("Token 无效或已过期");
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new UnauthorizedException("用户不存在");
        }
        return UserResponse.from(user);
    }

    /**
     * 修改当前用户密码。
     *
     * @param authorizationHeader HTTP Authorization 头
     * @param request             包含原密码和新密码
     * @throws UnauthorizedException Token 无效或原密码错误时抛出
     */
    @Transactional
    public void changePassword(String authorizationHeader, ChangePasswordRequest request) {
        String token = extractBearerToken(authorizationHeader);
        Long userId;
        try {
            userId = jwtUtils.extractUserId(token);
        } catch (JwtException e) {
            throw new UnauthorizedException("Token 无效或已过期");
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new UnauthorizedException("用户不存在");
        }
        if (user.getPasswordHash() == null
                || !passwordEncoder.matches(request.oldPassword(), user.getPasswordHash())) {
            throw new UnauthorizedException("原密码不正确");
        }
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userMapper.updateById(user);
    }

    /**
     * 对明文密码进行 BCrypt 哈希，供 {@link UserService} 创建用户时调用。
     *
     * @param rawPassword 明文密码
     * @return BCrypt 哈希值
     */
    public String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private String extractBearerToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("缺少或格式错误的 Authorization 头");
        }
        return authorizationHeader.substring(7);
    }
} 