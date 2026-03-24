package com.aiplatform.backend.common.exception;

import org.springframework.http.HttpStatus;

/** 权限不足（403）。 */
public class ForbiddenException extends BusinessException {

    public ForbiddenException(String message) {
        super(HttpStatus.FORBIDDEN.value(), BizErrorCode.FORBIDDEN, message);
    }
}
