package com.aiplatform.backend.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 用户不存在时抛出的异常。
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class UserNotFoundException extends RuntimeException {

    /**
     * 根据用户ID构造异常。
     *
     * @param userId 未找到的用户ID
     */
    public UserNotFoundException(Long userId) {
        super("User not found: " + userId);
    }
}
