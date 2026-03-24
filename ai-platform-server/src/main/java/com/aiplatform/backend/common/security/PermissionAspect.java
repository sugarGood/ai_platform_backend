package com.aiplatform.backend.common.security;

import com.aiplatform.backend.common.annotation.RequirePermission;
import com.aiplatform.backend.common.annotation.RequireRole;
import com.aiplatform.backend.common.exception.ForbiddenException;
import com.aiplatform.backend.common.exception.UnauthorizedException;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

/**
 * 权限校验切面。
 *
 * <p>拦截标注了 {@link RequireRole} 或 {@link RequirePermission} 注解的方法，
 * 从 {@link AuthContext} 读取当前用户信息并进行鉴权。</p>
 *
 * <p>执行顺序：{@code AuthInterceptor}（认证）→ {@code PermissionAspect}（鉴权）</p>
 *
 * <p>注解可用于方法或类：
 * <ul>
 *   <li>标注在类上时，类内所有方法均受限制</li>
 *   <li>方法注解优先级高于类注解</li>
 * </ul>
 */
@Aspect
@Component
public class PermissionAspect {

    /**
     * 校验 {@link RequireRole} 注解。
     *
     * <p>当前用户的 {@code platformRole} 须在注解声明的角色列表中（OR 逻辑）。</p>
     *
     * @param requireRole 角色注解
     * @throws UnauthorizedException 用户未登录（AuthContext 为空）
     * @throws ForbiddenException    角色不满足要求
     */
    @Before("@annotation(requireRole)")
    public void checkRole(RequireRole requireRole) {
        if (!AuthContext.isAuthenticated()) {
            throw new UnauthorizedException("请先登录");
        }
        if (!AuthContext.hasRole(requireRole.value())) {
            throw new ForbiddenException(
                    "需要以下角色之一: " + String.join(", ", requireRole.value()) +
                    "，当前角色: " + AuthContext.getRole());
        }
    }

    /**
     * 校验 {@link RequirePermission} 注解。
     *
     * <p>根据 {@link RequirePermission#any()} 决定 OR / AND 逻辑：
     * <ul>
     *   <li>{@code any=true}：满足其中任意一个权限即可放行</li>
     *   <li>{@code any=false}（默认）：需要满足全部权限</li>
     * </ul>
     *
     * <p>SUPER_ADMIN 权限集合包含通配符 {@code *}，自动跳过细粒度检查。</p>
     *
     * @param requirePermission 权限注解
     * @throws UnauthorizedException 用户未登录
     * @throws ForbiddenException    权限不足
     */
    @Before("@annotation(requirePermission)")
    public void checkPermission(RequirePermission requirePermission) {
        if (!AuthContext.isAuthenticated()) {
            throw new UnauthorizedException("请先登录");
        }

        AuthContext.AuthPrincipal principal = AuthContext.get();

        // SUPER_ADMIN 拥有通配符权限，直接放行
        if (principal.permissions().contains("*")) {
            return;
        }

        String[] required = requirePermission.value();
        boolean any = requirePermission.any();

        if (any) {
            // OR：满足其中任意一个
            for (String code : required) {
                if (AuthContext.hasPermission(code)) return;
            }
            throw new ForbiddenException(
                    "缺少权限（满足其一即可）: " + String.join(", ", required));
        } else {
            // AND：需要全部满足
            if (!AuthContext.hasAllPermissions(required)) {
                throw new ForbiddenException(
                        "缺少权限（需全部满足）: " + String.join(", ", required));
            }
        }
    }
}
