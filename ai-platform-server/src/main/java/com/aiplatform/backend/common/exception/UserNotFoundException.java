package com.aiplatform.backend.common.exception;

import org.springframework.http.HttpStatus;

/** 用户不存在。 */
public class UserNotFoundException extends BusinessException {

    public UserNotFoundException(Long userId) {
        super(HttpStatus.NOT_FOUND.value(), BizErrorCode.USER_NOT_FOUND, "用户不存在: " + userId);
    }
}
