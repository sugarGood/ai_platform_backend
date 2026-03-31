package com.aiplatform.backend.common.security;

import cn.dev33.satoken.stp.StpUtil;
import com.aiplatform.backend.entity.User;
import com.aiplatform.backend.mapper.RoleMapper;
import com.aiplatform.backend.mapper.UserMapper;
import com.aiplatform.backend.service.RbacService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Set;

/**
 * 在 Sa-Token 校验通过后，将当前用户与 RBAC 权限写入 {@link AuthContext}，供业务与 {@code PermissionAspect} 使用。
 */
@Component
public class AuthContextLoadInterceptor implements HandlerInterceptor {

    private final RbacService rbacService;
    private final UserMapper userMapper;
    private final RoleMapper roleMapper;

    public AuthContextLoadInterceptor(RbacService rbacService,
                                      UserMapper userMapper,
                                      RoleMapper roleMapper) {
        this.rbacService = rbacService;
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {
        if (!StpUtil.isLogin()) {
            return true;
        }

        long userId = StpUtil.getLoginIdAsLong();
        User user = userMapper.selectById(userId);
        if (user == null || "DISABLED".equals(user.getStatus())) {
            StpUtil.logout(userId);
            AuthContext.clear();
            return true;
        }

        String platformRole;
        if (user.getRoleId() != null) {
            platformRole = roleMapper.findCodeById(user.getRoleId());
            if (platformRole == null) {
                platformRole = user.getPlatformRole();
            }
        } else {
            platformRole = user.getPlatformRole();
        }
        if (platformRole == null) {
            platformRole = "MEMBER";
        }

        Set<String> permissions = rbacService.getPermissions(platformRole);
        AuthContext.set(new AuthContext.AuthPrincipal(userId, user.getEmail(), platformRole, permissions));
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler,
                                Exception ex) {
        AuthContext.clear();
    }
}
