package com.aiplatform.backend.common.exception;

import org.springframework.http.HttpStatus;

/** 权限点不存在。 */
public class PermissionNotFoundException extends BusinessException {

    public PermissionNotFoundException(Long permissionId) {
        super(HttpStatus.NOT_FOUND.value(), BizErrorCode.PERMISSION_NOT_FOUND,
                "权限不存在: " + permissionId);
    }
}
