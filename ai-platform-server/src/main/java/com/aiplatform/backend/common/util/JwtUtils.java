package com.aiplatform.backend.common.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 生成与解析工具。
 *
 * <p>使用 HS256 算法签名，密钥和过期时间通过配置注入。</p>
 */
@Component
public class JwtUtils {

    private final SecretKey secretKey;
    private final long accessTokenTtlMs;
    private final long refreshTokenTtlMs;

    public JwtUtils(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-ttl-seconds:86400}") long accessTokenTtlSeconds,
            @Value("${jwt.refresh-token-ttl-seconds:604800}") long refreshTokenTtlSeconds) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenTtlMs = accessTokenTtlSeconds * 1000;
        this.refreshTokenTtlMs = refreshTokenTtlSeconds * 1000;
    }

    /**
     * 生成 Access Token。
     *
     * @param userId       用户ID
     * @param email        用户邮箱
     * @param platformRole 平台角色
     * @return 签名后的 JWT 字符串
     */
    public String generateAccessToken(Long userId, String email, String platformRole) {
        return buildToken(userId, email, platformRole, accessTokenTtlMs);
    }

    /**
     * 生成 Refresh Token（不携带角色信息，仅含 sub）。
     *
     * @param userId 用户ID
     * @param email  用户邮箱
     * @return 签名后的 JWT 字符串
     */
    public String generateRefreshToken(Long userId, String email) {
        return buildToken(userId, email, null, refreshTokenTtlMs);
    }

    /**
     * 解析并校验 Token，返回 Claims。
     *
     * @param token JWT 字符串
     * @return 解析后的 Claims
     * @throws JwtException Token 无效或已过期时抛出
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 从 Token 中提取用户ID。
     *
     * @param token JWT 字符串
     * @return 用户ID
     */
    public Long extractUserId(String token) {
        return parseToken(token).get("uid", Long.class);
    }

    /**
     * 校验 Token 是否有效（签名正确且未过期）。
     *
     * @param token JWT 字符串
     * @return {@code true} 表示有效
     */
    public boolean isValid(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private String buildToken(Long userId, String email, String platformRole, long ttlMs) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + ttlMs);

        var builder = Jwts.builder()
                .subject(email)
                .claim("uid", userId)
                .issuedAt(now)
                .expiration(expiry);

        if (platformRole != null) {
            builder.claim("role", platformRole);
        }

        return builder.signWith(secretKey).compact();
    }
}
