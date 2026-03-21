package com.aiplatform.backend.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 上游 API 密钥不存在时抛出的异常。
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ProviderApiKeyNotFoundException extends RuntimeException {

    /**
     * 根据密钥ID构造异常。
     *
     * @param keyId 未找到的密钥ID
     */
    public ProviderApiKeyNotFoundException(Long keyId) {
        super("Provider API key not found: " + keyId);
    }
}
