package com.aiplatform.backend.dto.me;

/**
 * 我的凭证测试响应。
 *
 * @param success      是否连通
 * @param message      返回说明
 * @param credentialId 当前用户凭证 ID
 */
public record MeCredentialTestResponse(
        Boolean success,
        String message,
        Long credentialId
) {
}
