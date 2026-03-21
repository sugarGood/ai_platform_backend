package com.aiplatform.backend.dto;

/**
 * 创建凭证后的响应，包含明文密钥（仅此一次展示，后续无法再获取）。
 *
 * @param plainKey   明文密钥，格式为 plt_{uid}_{random}_{checksum}
 * @param credential 凭证详情
 */
public record CreatePlatformCredentialResponse(
        String plainKey,
        PlatformCredentialResponse credential
) {
}
