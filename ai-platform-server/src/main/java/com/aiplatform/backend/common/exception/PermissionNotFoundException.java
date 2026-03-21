package com.aiplatform.backend.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 权限点不存在异常。
 *
 * <p>当根据 ID 查询权限点但未找到记录时抛出，HTTP 状态码返回 404。</p>
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class PermissionNotFoundException extends RuntimeException {

    /**
     * @param permissionId 未找到的权限点 ID
     */
    public PermissionNotFoundException(Long permissionId) {
        super("Permission not found: " + permissionId);
    }
}
