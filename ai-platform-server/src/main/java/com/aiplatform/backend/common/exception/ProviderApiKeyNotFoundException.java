package com.aiplatform.backend.common.exception;

import org.springframework.http.HttpStatus;

/** 供应商 API Key 不存在。 */
public class ProviderApiKeyNotFoundException extends BusinessException {

    public ProviderApiKeyNotFoundException(Long keyId) {
        super(HttpStatus.NOT_FOUND.value(), BizErrorCode.PROVIDER_API_KEY_NOT_FOUND,
                "API Key 不存在: " + keyId);
    }
}
