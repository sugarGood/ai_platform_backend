package com.aiplatform.backend.dto.me;

/**
 * 我的凭证连通性测试请求。
 *
 * @param clientAppId 客户端 ID，若为空则更新当前用户全部 ACTIVE 绑定
 */
public record MeCredentialTestRequest(
        Long clientAppId
) {
}
