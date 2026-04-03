package com.aiplatform.backend.dto.me;

import jakarta.validation.constraints.Min;

/**
 * 我的凭证续签请求。
 *
 * @param renewDays 续签天数
 */
public record MeCredentialRenewRequest(
        @Min(value = 1, message = "renewDays must be greater than 0")
        Integer renewDays
) {
}
