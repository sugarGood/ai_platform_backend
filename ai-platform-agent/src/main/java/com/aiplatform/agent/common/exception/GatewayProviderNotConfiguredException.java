package com.aiplatform.agent.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 网关未配置对应上游服务商时抛出的异常。
 */
@ResponseStatus(HttpStatus.BAD_GATEWAY)
public class GatewayProviderNotConfiguredException extends RuntimeException {

    /**
     * 构造服务商未配置异常。
     *
     * @param provider 服务商编码
     */
    public GatewayProviderNotConfiguredException(String provider) {
        super("Gateway provider is not configured: " + provider);
    }
}
