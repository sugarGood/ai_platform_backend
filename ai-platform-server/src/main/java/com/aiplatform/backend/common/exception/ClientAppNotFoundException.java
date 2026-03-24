package com.aiplatform.backend.common.exception;

import org.springframework.http.HttpStatus;

/** 客户端应用不存在。 */
public class ClientAppNotFoundException extends BusinessException {

    public ClientAppNotFoundException(Long clientAppId) {
        super(HttpStatus.NOT_FOUND.value(), BizErrorCode.CLIENT_APP_NOT_FOUND,
                "客户端应用不存在: " + clientAppId);
    }
}
