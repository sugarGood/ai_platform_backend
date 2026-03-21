package com.aiplatform.backend.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 故障转移策略不存在时抛出的异常。
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ProviderFailoverPolicyNotFoundException extends RuntimeException {

    /**
     * 根据策略ID构造异常。
     *
     * @param policyId 未找到的策略ID
     */
    public ProviderFailoverPolicyNotFoundException(Long policyId) {
        super("Provider failover policy not found: " + policyId);
    }
}
