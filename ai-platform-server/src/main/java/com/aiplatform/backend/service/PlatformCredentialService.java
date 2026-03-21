package com.aiplatform.backend.service;

import com.aiplatform.backend.common.exception.PlatformCredentialNotFoundException;
import com.aiplatform.backend.dto.CreatePlatformCredentialRequest;
import com.aiplatform.backend.dto.CreatePlatformCredentialResponse;
import com.aiplatform.backend.dto.PlatformCredentialResponse;
import com.aiplatform.backend.entity.PlatformCredential;
import com.aiplatform.backend.mapper.PlatformCredentialMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

/**
 * 平台凭证业务服务。
 *
 * <p>提供凭证的创建（含密钥生成和 SHA-256 哈希）、按用户查询、吊销等操作。
 * 密钥格式为 {@code plt_{uid}_{random}_{checksum}}，创建时仅返回一次明文。</p>
 */
@Service
public class PlatformCredentialService {

    private final PlatformCredentialMapper platformCredentialMapper;

    /** 构造函数。 */
    public PlatformCredentialService(PlatformCredentialMapper platformCredentialMapper) {
        this.platformCredentialMapper = platformCredentialMapper;
    }

    /**
     * 创建平台凭证，生成密钥并以 SHA-256 哈希存储。
     *
     * @param request 创建请求
     * @return 包含明文密钥的创建响应（明文仅此一次展示）
     */
    public CreatePlatformCredentialResponse create(CreatePlatformCredentialRequest request) {
        String rawKey = "plt_" + request.userId() + "_"
                + UUID.randomUUID().toString().replace("-", "").substring(0, 16)
                + "_" + checksumChar(request.userId());

        String keyHash = sha256Hex(rawKey);
        String keyPrefix = rawKey.substring(0, 12);

        PlatformCredential credential = new PlatformCredential();
        credential.setUserId(request.userId());
        credential.setCredentialType(request.credentialType() != null ? request.credentialType() : "PERSONAL");
        credential.setKeyHash(keyHash);
        credential.setKeyPrefix(keyPrefix);
        credential.setName(request.name());
        credential.setBoundProjectId(request.boundProjectId());
        credential.setStatus("ACTIVE");
        platformCredentialMapper.insert(credential);

        return new CreatePlatformCredentialResponse(rawKey, PlatformCredentialResponse.from(credential));
    }

    /** 按用户 ID 查询凭证列表。 */
    public List<PlatformCredential> listByUserId(Long userId) {
        return platformCredentialMapper.selectList(
                Wrappers.<PlatformCredential>lambdaQuery()
                        .eq(PlatformCredential::getUserId, userId)
                        .orderByAsc(PlatformCredential::getId)
        );
    }

    /**
     * 吊销凭证，设置状态为 REVOKED 并记录原因。
     *
     * @param id     凭证 ID
     * @param reason 吊销原因
     */
    public void revoke(Long id, String reason) {
        PlatformCredential credential = getByIdOrThrow(id);
        credential.setStatus("REVOKED");
        credential.setRevokedAt(LocalDateTime.now());
        credential.setRevokeReason(reason);
        platformCredentialMapper.updateById(credential);
    }

    /** 根据 ID 查询凭证，不存在则抛出异常。 */
    public PlatformCredential getByIdOrThrow(Long id) {
        PlatformCredential credential = platformCredentialMapper.selectById(id);
        if (credential == null) {
            throw new PlatformCredentialNotFoundException(id);
        }
        return credential;
    }

    private String sha256Hex(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    private char checksumChar(Long userId) {
        int value = (int) (userId % 26);
        return (char) ('a' + value);
    }
}
