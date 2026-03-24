package com.aiplatform.backend.common.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 声明该方法需要指定的细粒度权限点才能访问。
 *
 * <p>示例：
 * <pre>{@code
 * @RequirePermission("user:create")
 * public CreateUserResponse create(...) { ... }
 *
 * @RequirePermission({"project:edit", "project:delete"})
 * public void deleteProject(Long id) { ... }
 * }</pre>
 *
 * <p>权限点格式为 {@code 模块:操作}，由 {@code permission_definitions} 表定义。
 * 校验逻辑由 {@code PermissionAspect} 实现，权限集合从 {@code RbacService} 加载。</p>
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequirePermission {

    /**
     * 需要的权限编码列表（默认需要全部满足，可通过 {@link #any()} 改为满足其一）。
     */
    String[] value();

    /**
     * {@code true}  表示满足 value 中任意一个权限即可放行（OR 逻辑）。
     * {@code false} 表示需要满足 value 中全部权限（AND 逻辑，默认）。
     */
    boolean any() default false;
}
