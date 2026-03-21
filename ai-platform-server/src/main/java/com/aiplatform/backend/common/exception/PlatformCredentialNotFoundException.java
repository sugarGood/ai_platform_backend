package com.aiplatform.backend.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 平台凭证不存在异常。
 *
 * <p>当根据 ID 查询凭证但未找到记录时抛出，HTTP 状态码返回 404。</p>
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class PlatformCredentialNotFoundException extends RuntimeException {

    /**
     * @param credentialId 未找到的凭证 ID
     */
    public PlatformCredentialNotFoundException(Long credentialId) {
        super("Platform credential not found: " + credentialId);
    }
}
