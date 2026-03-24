package com.aiplatform.backend.common.exception;

import org.springframework.http.HttpStatus;

/** 未认证或凭证无效（401）。 */
public class UnauthorizedException extends BusinessException {

    public UnauthorizedException(String message) {
        super(HttpStatus.UNAUTHORIZED.value(), BizErrorCode.UNAUTHORIZED, message);
    }
}
