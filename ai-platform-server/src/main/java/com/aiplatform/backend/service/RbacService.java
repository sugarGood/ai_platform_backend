package com.aiplatform.backend.service;

import com.aiplatform.backend.mapper.PermissionDefinitionMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * RBAC 权限查询服务。
 *
 * <p>根据角色编码查询其拥有的权限点集合，内置本地缓存避免每次请求都查库。
 * 缓存在应用启动后首次访问时加载，可通过 {@link #clearCache()} 手动刷新。</p>
 *
 * <p>SUPER_ADMIN 角色固定拥有全部权限（通配符 {@code *}），无需查库。</p>
 */
@Service
public class RbacService {

    /** 表示拥有全部权限的通配符，SUPER_ADMIN 专用 */
    public static final String WILDCARD = "*";

    private final PermissionDefinitionMapper permissionDefinitionMapper;

    /** 本地权限缓存：roleCode → Set<permissionCode> */
    private final Map<String, Set<String>> cache = new ConcurrentHashMap<>();

    public RbacService(PermissionDefinitionMapper permissionDefinitionMapper) {
        this.permissionDefinitionMapper = permissionDefinitionMapper;
    }

    /**
     * 获取指定角色的权限点集合。
     *
     * <p>SUPER_ADMIN 直接返回包含通配符 {@code *} 的集合，其余角色查数据库（带本地缓存）。</p>
     *
     * @param roleCode 角色编码
     * @return 权限编码集合（不可变）
     */
    public Set<String> getPermissions(String roleCode) {
        if ("SUPER_ADMIN".equals(roleCode)) {
            return Set.of(WILDCARD);
        }
        return cache.computeIfAbsent(roleCode, this::loadFromDb);
    }

    /**
     * 清除本地权限缓存（角色权限变更后调用）。
     */
    public void clearCache() {
        cache.clear();
    }

    /**
     * 清除指定角色的缓存条目。
     *
     * @param roleCode 角色编码
     */
    public void clearCache(String roleCode) {
        cache.remove(roleCode);
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private Set<String> loadFromDb(String roleCode) {
        List<String> codes = permissionDefinitionMapper.findPermissionCodesByRoleCode(roleCode);
        return codes == null ? Set.of() : new HashSet<>(codes);
    }
}
