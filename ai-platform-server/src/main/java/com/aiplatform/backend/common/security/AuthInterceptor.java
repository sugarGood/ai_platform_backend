package com.aiplatform.backend.common.security;

import com.aiplatform.backend.common.util.JwtUtils;
import com.aiplatform.backend.mapper.RoleMapper;
import com.aiplatform.backend.mapper.UserMapper;
import com.aiplatform.backend.entity.User;
import com.aiplatform.backend.service.RbacService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Set;

/**
 * JWT 认证拦截器。
 *
 * <p>对每个请求执行以下流程：
 * <ol>
 *   <li>从 {@code Authorization: Bearer <token>} 头中提取 JWT</li>
 *   <li>验证签名和有效期</li>
 *   <li>从 DB 查询用户最新的 {@code role_id}，再通过 {@code roles.code} 获取角色编码</li>
 *   <li>通过 {@link RbacService} 加载该角色的权限点集合</li>
 *   <li>将 {@link AuthContext.AuthPrincipal} 写入 {@link AuthContext}（ThreadLocal）</li>
 * </ol>
 *
 * <p>白名单路径（登录、刷新 Token、Swagger 文档等）跳过校验，直接放行。</p>
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    /** 不需要认证的路径前缀 */
    private static final String[] WHITE_LIST = {
            "/api/auth/login",
            "/api/auth/refresh",
            "/v3/api-docs",
            "/swagger-ui",
            "/swagger-ui.html",
            "/actuator"
    };

    private final JwtUtils jwtUtils;
    private final RbacService rbacService;
    private final UserMapper userMapper;
    private final RoleMapper roleMapper;

    public AuthInterceptor(JwtUtils jwtUtils, RbacService rbacService,
                           UserMapper userMapper, RoleMapper roleMapper) {
        this.jwtUtils = jwtUtils;
        this.rbacService = rbacService;
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        String path = request.getRequestURI();

        // 白名单直接放行
        if (isWhitelisted(path)) {
            return true;
        }

        // 提取 Bearer Token
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "缺少 Authorization 头");
            return false;
        }
        String token = authHeader.substring(7);

        // 解析并校验 Token
        Claims claims;
        try {
            claims = jwtUtils.parseToken(token);
        } catch (JwtException e) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token 无效或已过期");
            return false;
        }

        Long userId = claims.get("uid", Long.class);
        String email = claims.getSubject();
        if (userId == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token 内容不完整");
            return false;
        }

        // 从 DB 查用户最新 role_id，防止角色变更后 Token 旧缓存问题
        User user = userMapper.selectById(userId);
        if (user == null || "DISABLED".equals(user.getStatus())) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "用户不存在或已禁用");
            return false;
        }

        // 通过 role_id 查 roles.code 获取最新角色编码
        String platformRole;
        if (user.getRoleId() != null) {
            platformRole = roleMapper.findCodeById(user.getRoleId());
            if (platformRole == null) {
                // role_id 对应的角色被禁用，降级使用枚举字段
                platformRole = user.getPlatformRole();
            }
        } else {
            // 兼容旧数据：role_id 未设置时使用枚举字段
            platformRole = user.getPlatformRole();
        }

        if (platformRole == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "用户角色配置异常");
            return false;
        }

        // 加载权限集合并写入 AuthContext
        Set<String> permissions = rbacService.getPermissions(platformRole);
        AuthContext.set(new AuthContext.AuthPrincipal(userId, email, platformRole, permissions));

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler,
                                Exception ex) {
        AuthContext.clear();
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private boolean isWhitelisted(String path) {
        for (String prefix : WHITE_LIST) {
            if (path.startsWith(prefix)) return true;
        }
        return false;
    }
}
