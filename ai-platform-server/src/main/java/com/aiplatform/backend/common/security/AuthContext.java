package com.aiplatform.backend.common.security;

import java.util.Collections;
import java.util.Set;

/**
 * 当前请求的认证上下文，通过 {@link ThreadLocal} 在请求生命周期内传递用户信息。
 *
 * <p>由 {@link AuthContextLoadInterceptor} 在 Sa-Token 校验通过后写入，请求结束时清理（防止线程池复用泄漏）。</p>
 *
 * <p>使用示例：
 * <pre>{@code
 * // 获取当前用户 ID
 * Long userId = AuthContext.getUserId();
 *
 * // 判断是否有某角色
 * if (AuthContext.hasRole("SUPER_ADMIN")) { ... }
 *
 * // 判断是否有某权限
 * if (AuthContext.hasPermission("user:create")) { ... }
 * }</pre>
 */
public final class AuthContext {

    private static final ThreadLocal<AuthPrincipal> HOLDER = new ThreadLocal<>();

    private AuthContext() {}

    /** 写入当前请求的认证主体（由拦截器调用）。 */
    public static void set(AuthPrincipal principal) {
        HOLDER.set(principal);
    }

    /** 获取当前认证主体，未认证时返回 {@code null}。 */
    public static AuthPrincipal get() {
        return HOLDER.get();
    }

    /** 清理 ThreadLocal，防止线程池复用时数据污染（拦截器 afterCompletion 中调用）。 */
    public static void clear() {
        HOLDER.remove();
    }

    /** 当前用户是否已认证。 */
    public static boolean isAuthenticated() {
        return HOLDER.get() != null;
    }

    /** 获取当前用户 ID，未认证时返回 {@code null}。 */
    public static Long getUserId() {
        AuthPrincipal p = HOLDER.get();
        return p == null ? null : p.userId();
    }

    /** 获取当前用户邮箱，未认证时返回 {@code null}。 */
    public static String getEmail() {
        AuthPrincipal p = HOLDER.get();
        return p == null ? null : p.email();
    }

    /** 获取当前用户平台角色，未认证时返回 {@code null}。 */
    public static String getRole() {
        AuthPrincipal p = HOLDER.get();
        return p == null ? null : p.platformRole();
    }

    /**
     * 判断当前用户是否拥有指定角色之一。
     *
     * @param roles 角色编码列表
     * @return 拥有其中任意一个角色返回 {@code true}
     */
    public static boolean hasRole(String... roles) {
        String current = getRole();
        if (current == null) return false;
        for (String r : roles) {
            if (current.equals(r)) return true;
        }
        return false;
    }

    /**
     * 判断当前用户是否拥有指定权限点。
     *
     * @param permissionCode 权限编码，如 {@code user:create}
     * @return 拥有该权限返回 {@code true}
     */
    public static boolean hasPermission(String permissionCode) {
        AuthPrincipal p = HOLDER.get();
        if (p == null) return false;
        return p.permissions().contains(permissionCode);
    }

    /**
     * 判断当前用户是否拥有全部指定权限点。
     *
     * @param permissionCodes 权限编码数组
     * @return 全部拥有返回 {@code true}
     */
    public static boolean hasAllPermissions(String... permissionCodes) {
        AuthPrincipal p = HOLDER.get();
        if (p == null) return false;
        Set<String> owned = p.permissions();
        for (String code : permissionCodes) {
            if (!owned.contains(code)) return false;
        }
        return true;
    }

    /**
     * 当前请求认证主体（不可变值对象）。
     *
     * @param userId       用户 ID
     * @param email        用户邮箱
     * @param platformRole 平台角色编码
     * @param permissions  该角色对应的权限点集合
     */
    public record AuthPrincipal(
            Long userId,
            String email,
            String platformRole,
            Set<String> permissions
    ) {
        public AuthPrincipal {
            permissions = permissions == null ? Collections.emptySet() : Collections.unmodifiableSet(permissions);
        }
    }
}
