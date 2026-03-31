package com.aiplatform.backend.service;

import cn.dev33.satoken.stp.StpUtil;
import com.aiplatform.backend.common.exception.UnauthorizedException;
import com.aiplatform.backend.common.exception.UserNotFoundException;
import com.aiplatform.backend.dto.ChangePasswordRequest;
import com.aiplatform.backend.dto.LoginRequest;
import com.aiplatform.backend.dto.LoginResponse;
import com.aiplatform.backend.dto.UserResponse;
import com.aiplatform.backend.entity.User;
import com.aiplatform.backend.mapper.UserMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 认证业务服务。
 *
 * <p>登录态由 Sa-Token 管理；密码仍使用 BCrypt。</p>
 */
@Service
public class AuthService {

    private final UserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder;

    @Value("${sa-token.timeout:86400}")
    private long tokenTimeoutSeconds;

    public AuthService(UserMapper userMapper) {
        this.userMapper = userMapper;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    /**
     * 用户登录，校验邮箱和密码后创建 Sa-Token 会话。
     *
     * @return 响应中 accessToken 与 refreshToken 均为当前会话 Token（前端可只存一份或沿用双字段结构）
     */
    public LoginResponse login(LoginRequest request) {
        User user = userMapper.selectOne(
                Wrappers.<User>lambdaQuery().eq(User::getEmail, request.email()));
        if (user == null) {
            throw new UnauthorizedException("邮箱或密码不正确");
        }

        if (user.getPasswordHash() == null
                || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new UnauthorizedException("邮箱或密码不正确");
        }

        if ("DISABLED".equals(user.getStatus())) {
            throw new UnauthorizedException("账号已被禁用，请联系管理员");
        }
        if ("INACTIVE".equals(user.getStatus())) {
            throw new UnauthorizedException("账号尚未激活，请先完成邮件验证");
        }

        StpUtil.login(user.getId());
        String token = StpUtil.getTokenValue();
        return LoginResponse.of(token, token, tokenTimeoutSeconds, UserResponse.from(user));
    }

    /**
     * 使用 Refresh Token（与 Access 相同，均为 Sa-Token 值）轮换会话：校验旧 Token 后注销并重新登录。
     */
    public LoginResponse refresh(String refreshToken) {
        Object loginId = StpUtil.getLoginIdByToken(refreshToken);
        if (loginId == null) {
            throw new UnauthorizedException("Refresh Token 无效或已过期");
        }
        long userId = loginId instanceof Long l ? l : Long.parseLong(loginId.toString());
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new UnauthorizedException("用户不存在");
        }
        if ("DISABLED".equals(user.getStatus())) {
            throw new UnauthorizedException("账号已被禁用");
        }

        StpUtil.logoutByTokenValue(refreshToken);
        StpUtil.login(user.getId());
        String token = StpUtil.getTokenValue();
        return LoginResponse.of(token, token, tokenTimeoutSeconds, UserResponse.from(user));
    }

    public UserResponse getCurrentUser() {
        StpUtil.checkLogin();
        long userId = StpUtil.getLoginIdAsLong();
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new UserNotFoundException(userId);
        }
        return UserResponse.from(user);
    }

    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        StpUtil.checkLogin();
        long userId = StpUtil.getLoginIdAsLong();
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
     */
    public String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }
}
