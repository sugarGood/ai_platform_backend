package com.aiplatform.backend.common.exception;

import org.springframework.http.HttpStatus;

/** 角色不存在。 */
public class RoleNotFoundException extends BusinessException {

    public RoleNotFoundException(Long roleId) {
        super(HttpStatus.NOT_FOUND.value(), BizErrorCode.ROLE_NOT_FOUND, "角色不存在: " + roleId);
    }
}
