package com.aiplatform.agent.gateway.service;

import com.aiplatform.agent.gateway.entity.PlatformCredentialRef;
import com.aiplatform.agent.gateway.mapper.PlatformCredentialRefMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;

/**
 * 凭证认证服务。
 *
 * <p>负责解析请求头中的 Bearer Token，通过 SHA-256 哈希匹配数据库中的有效凭证，
 * 完成调用方身份认证。认证失败或凭证过期时将抛出 {@link InvalidCredentialException}。</p>
 */
@Service
public class CredentialAuthService {

    /** 平台凭证数据访问 Mapper */
    private final PlatformCredentialRefMapper credentialMapper;

    /**
     * 构造凭证认证服务。
     *
     * @param credentialMapper 平台凭证数据访问 Mapper
     */
    public CredentialAuthService(PlatformCredentialRefMapper credentialMapper) {
        this.credentialMapper = credentialMapper;
    }

    /**
     * 对 Bearer Token 进行认证。
     *
     * <p>提取 Token 明文，计算其 SHA-256 哈希值，在数据库中查找匹配的有效凭证。
     * 若凭证不存在、已撤销或已过期，将抛出异常。</p>
     *
     * @param bearerToken 请求头中的 Authorization 值（格式：Bearer xxx）
     * @return 匹配到的有效平台凭证
     * @throws InvalidCredentialException 凭证无效、已撤销或已过期时抛出
     */
    public PlatformCredentialRef authenticate(String bearerToken) {
        if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
            throw new InvalidCredentialException("Missing or invalid Bearer token");
        }
        String token = bearerToken.substring(7).trim();
        String hash = sha256(token);

        PlatformCredentialRef credential = credentialMapper.selectOne(
                Wrappers.<PlatformCredentialRef>lambdaQuery()
                        .eq(PlatformCredentialRef::getKeyHash, hash)
                        .eq(PlatformCredentialRef::getStatus, "ACTIVE")
                        .last("LIMIT 1")
        );
        if (credential == null) {
            throw new InvalidCredentialException("Invalid or revoked credential");
        }
        if (credential.getExpiresAt() != null && credential.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InvalidCredentialException("Credential has expired");
        }
        return credential;
    }

    /**
     * 计算字符串的 SHA-256 哈希值。
     *
     * @param input 待哈希的原始字符串
     * @return 十六进制编码的哈希值
     */
    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    /**
     * 凭证无效异常。
     *
     * <p>当提供的 Token 缺失、格式错误、已撤销或已过期时抛出。</p>
     */
    public static class InvalidCredentialException extends RuntimeException {
        /**
         * 构造凭证无效异常。
         *
         * @param message 错误描述信息
         */
        public InvalidCredentialException(String message) {
            super(message);
        }
    }
}
