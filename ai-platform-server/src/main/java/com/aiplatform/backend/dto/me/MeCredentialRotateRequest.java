package com.aiplatform.backend.dto.me;

import jakarta.validation.constraints.Min;

/**
 * 我的凭证轮换请求。
 *
 * @param gracePeriodHours 新旧密钥并行宽限期（小时）
 */
public record MeCredentialRotateRequest(
        @Min(value = 1, message = "gracePeriodHours must be greater than 0")
        Integer gracePeriodHours
) {
}
