package com.aiplatform.backend.common.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 声明该方法或类需要指定的平台角色之一才能访问。
 *
 * <p>示例：
 * <pre>{@code
 * // 只有 SUPER_ADMIN 可访问
 * @RequireRole("SUPER_ADMIN")
 * public void deleteUser(Long id) { ... }
 *
 * // SUPER_ADMIN 或 PLATFORM_ADMIN 均可访问
 * @RequireRole({"SUPER_ADMIN", "PLATFORM_ADMIN"})
 * public List<User> listUsers() { ... }
 * }</pre>
 *
 * <p>校验逻辑由 {@code PermissionAspect} 实现，用户角色从 {@code AuthContext} 读取。</p>
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequireRole {

    /**
     * 允许访问的角色编码列表（满足其一即可）。
     * 对应 {@code users.platform_role} 枚举值，如 SUPER_ADMIN、PLATFORM_ADMIN、MEMBER。
     */
    String[] value();
}
