package com.aiplatform.backend.common.exception;

import org.springframework.http.HttpStatus;

/** 供应商故障切换策略不存在。 */
public class ProviderFailoverPolicyNotFoundException extends BusinessException {

    public ProviderFailoverPolicyNotFoundException(Long policyId) {
        super(HttpStatus.NOT_FOUND.value(), BizErrorCode.PROVIDER_FAILOVER_POLICY_NOT_FOUND,
                "故障切换策略不存在: " + policyId);
    }
}
