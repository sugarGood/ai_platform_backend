package com.aiplatform.backend.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 角色不存在异常。
 *
 * <p>当根据ID查询角色但未找到对应记录时抛出此异常，
 * HTTP 状态码返回 404 Not Found。</p>
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class RoleNotFoundException extends RuntimeException {

    /**
     * 构造函数。
     *
     * @param roleId 未找到的角色ID
     */
    public RoleNotFoundException(Long roleId) {
        super("Role not found: " + roleId);
    }
}
