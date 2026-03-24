package com.aiplatform.backend.common.exception;

import org.springframework.http.HttpStatus;

/** 平台凭证不存在。 */
public class PlatformCredentialNotFoundException extends BusinessException {

    public PlatformCredentialNotFoundException(Long credentialId) {
        super(HttpStatus.NOT_FOUND.value(), BizErrorCode.PLATFORM_CREDENTIAL_NOT_FOUND,
                "平台凭证不存在: " + credentialId);
    }
}
