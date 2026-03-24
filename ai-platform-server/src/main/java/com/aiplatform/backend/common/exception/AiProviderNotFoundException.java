package com.aiplatform.backend.common.exception;

import org.springframework.http.HttpStatus;

/** AI 供应商不存在。 */
public class AiProviderNotFoundException extends BusinessException {

    public AiProviderNotFoundException(Long providerId) {
        super(HttpStatus.NOT_FOUND.value(), BizErrorCode.AI_PROVIDER_NOT_FOUND,
                "AI 供应商不存在: " + providerId);
    }
}
