package com.aiplatform.backend.service;

import com.aiplatform.backend.common.exception.PlatformCredentialNotFoundException;
import com.aiplatform.backend.common.exception.UnauthorizedException;
import com.aiplatform.backend.dto.CreatePlatformCredentialRequest;
import com.aiplatform.backend.dto.CreatePlatformCredentialResponse;
import com.aiplatform.backend.dto.KeyRotationLogResponse;
import com.aiplatform.backend.dto.PlatformCredentialResponse;
import com.aiplatform.backend.entity.KeyRotationLog;
import com.aiplatform.backend.entity.PlatformCredential;
import com.aiplatform.backend.mapper.KeyRotationLogMapper;
import com.aiplatform.backend.mapper.PlatformCredentialMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
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
 *
 * <h3>凭证模型：一人一证，跨项目共用</h3>
 * <p>每名成员在平台拥有唯一一张凭证（uk_platform_credentials_user 唯一索引约束），
 * 邀请时自动生成。凭证跨项目复用，无需为不同项目单独申请。</p>
 */
@Service
public class PlatformCredentialService {

    private final PlatformCredentialMapper platformCredentialMapper;
    private final KeyRotationLogMapper keyRotationLogMapper;

    public PlatformCredentialService(PlatformCredentialMapper platformCredentialMapper,
                                     KeyRotationLogMapper keyRotationLogMapper) {
        this.platformCredentialMapper = platformCredentialMapper;
        this.keyRotationLogMapper = keyRotationLogMapper;
    }

    /**
     * 创建平台凭证，生成密钥并以 SHA-256 哈希存储。
     *
     * <p>自动初始化个人月度 Token 配额字段（双池之一）。
     * 若请求未指定配额参数，使用平台默认值：上限 200K、阈值 80%、策略 BLOCK。</p>
     *
     * @param request 创建请求
     * @return 包含明文密钥的创建响应（明文仅此一次展示）
     */
    public CreatePlatformCredentialResponse create(CreatePlatformCredentialRequest request) {
        String rawKey = "plt_" + request.userId() + "_"
                + UUID.randomUUID().toString().replace("-", "").substring(0, 16)
                + "_" + checksumChar(request.userId());

        String keyHash = sha256Hex(rawKey);
        String keyPrefix = rawKey.substring(0, Math.min(12, rawKey.length()));

        PlatformCredential credential = new PlatformCredential();
        credential.setUserId(request.userId());
        credential.setCredentialType(request.credentialType() != null ? request.credentialType() : "PERSONAL");
        credential.setKeyHash(keyHash);
        credential.setKeyPrefix(keyPrefix);
        credential.setName(request.name() != null ? request.name() : "Personal Credential");

        // 个人月度 Token 配额初始化（双池之一）
        credential.setMonthlyTokenQuota(
                request.monthlyTokenQuota() != null ? request.monthlyTokenQuota() : 200000L);
        credential.setUsedTokensThisMonth(0L);
        credential.setAlertThresholdPct(
                request.alertThresholdPct() != null ? request.alertThresholdPct() : 80);
        credential.setOverQuotaStrategy(
                request.overQuotaStrategy() != null ? request.overQuotaStrategy() : "BLOCK");
        credential.setLastQuotaResetAt(LocalDateTime.now());

        credential.setStatus("ACTIVE");
        platformCredentialMapper.insert(credential);

        return new CreatePlatformCredentialResponse(rawKey, PlatformCredentialResponse.from(credential));
    }

    /** 按用户 ID 查询凭证（每用户唯一一条）。 */
    public List<PlatformCredential> listByUserId(Long userId) {
        return platformCredentialMapper.selectList(
                Wrappers.<PlatformCredential>lambdaQuery()
                        .eq(PlatformCredential::getUserId, userId)
                        .orderByAsc(PlatformCredential::getId)
        );
    }

    /** 按用户 ID 查询唯一凭证，不存在时返回 null。 */
    public PlatformCredential getByUserId(Long userId) {
        return platformCredentialMapper.selectOne(
                Wrappers.<PlatformCredential>lambdaQuery()
                        .eq(PlatformCredential::getUserId, userId)
        );
    }

    /**
     * 校验明文平台密钥属于指定用户且处于可用状态，返回可转发给 Agent 的 {@code Authorization} 头值。
     *
     * @param userId          当前登录用户（JWT）
     * @param rawOrBearerKey  请求头 {@code X-Platform-Key}，可为 {@code plt_...} 或 {@code Bearer plt_...}
     * @return 形如 {@code Bearer plt_...} 的完整 Authorization 头
     */
    public String authorizationHeaderForValidatedPlatformKey(Long userId, String rawOrBearerKey) {
        String raw = normalizePlatformKey(rawOrBearerKey);
        PlatformCredential credential = getByUserId(userId);
        if (credential == null) {
            throw new UnauthorizedException("未找到平台凭证，请先在平台创建个人凭证");
        }
        if (!"ACTIVE".equals(credential.getStatus())) {
            throw new UnauthorizedException("平台凭证不可用");
        }
        if (credential.getExpiresAt() != null && credential.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new UnauthorizedException("平台凭证已过期");
        }
        String hash = sha256Hex(raw);
        if (!hash.equalsIgnoreCase(credential.getKeyHash())) {
            throw new UnauthorizedException("X-Platform-Key 与当前用户凭证不匹配");
        }
        return "Bearer " + raw;
    }

    private static String normalizePlatformKey(String rawOrBearerKey) {
        if (rawOrBearerKey == null || rawOrBearerKey.isBlank()) {
            throw new UnauthorizedException("缺少 X-Platform-Key 请求头");
        }
        String t = rawOrBearerKey.trim();
        if (t.regionMatches(true, 0, "Bearer ", 0, 7)) {
            t = t.substring(7).trim();
        }
        if (t.isEmpty()) {
            throw new UnauthorizedException("X-Platform-Key 不能为空");
        }
        return t;
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

    /**
     * 批量吊销某用户名下所有 ACTIVE 状态的凭证（账号停用时调用）。
     */
    public void revokeAllByUserId(Long userId, String reason) {
        List<PlatformCredential> active = platformCredentialMapper.selectList(
                Wrappers.<PlatformCredential>lambdaQuery()
                        .eq(PlatformCredential::getUserId, userId)
                        .eq(PlatformCredential::getStatus, "ACTIVE")
        );
        for (PlatformCredential c : active) {
            c.setStatus("REVOKED");
            c.setRevokedAt(LocalDateTime.now());
            c.setRevokeReason(reason);
            platformCredentialMapper.updateById(c);
        }
    }

    /**
     * 续期凭证（延长过期时间）。
     *
     * @param id      凭证ID
     * @param renewDays 续期天数（30/90/180）
     * @return 更新后的凭证
     */
    public PlatformCredential renew(Long id, int renewDays) {
        PlatformCredential credential = getByIdOrThrow(id);
        LocalDateTime base = credential.getExpiresAt() != null && credential.getExpiresAt().isAfter(LocalDateTime.now())
                ? credential.getExpiresAt() : LocalDateTime.now();
        credential.setExpiresAt(base.plusDays(renewDays));
        platformCredentialMapper.updateById(credential);
        return credential;
    }

    /**
     * 轮换凭证：生成新密钥，旧密钥有 gracePeriodHours 小时宽限期后过期。
     *
     * @param id                凭证ID
     * @param gracePeriodHours  宽限期小时数，默认24
     * @return 含新明文密钥的响应
     */
    public CreatePlatformCredentialResponse rotate(Long id, int gracePeriodHours) {
        PlatformCredential credential = getByIdOrThrow(id);

        // 记录旧密钥日志
        KeyRotationLog log = new KeyRotationLog();
        log.setTargetType("PLATFORM_CREDENTIAL");
        log.setTargetId(id);
        log.setOldKeyPrefix(credential.getKeyPrefix());
        log.setRotationType("MANUAL");
        log.setOperatedBy(credential.getUserId());
        log.setResult("SUCCESS");
        keyRotationLogMapper.insert(log);

        // 生成新密钥
        String rawKey = "plt_" + credential.getUserId() + "_"
                + UUID.randomUUID().toString().replace("-", "").substring(0, 16)
                + "_" + checksumChar(credential.getUserId());
        credential.setKeyHash(sha256Hex(rawKey));
        credential.setKeyPrefix(rawKey.substring(0, Math.min(12, rawKey.length())));
        credential.setStatus("ACTIVE");
        platformCredentialMapper.updateById(credential);

        return new CreatePlatformCredentialResponse(rawKey, PlatformCredentialResponse.from(credential));
    }

    /**
     * 查询凭证的轮换日志列表。
     *
     * @param credentialId 凭证ID
     * @return 轮换日志响应列表
     */
    public List<KeyRotationLogResponse> listRotationLogs(Long credentialId) {
        List<KeyRotationLog> logs = keyRotationLogMapper.selectList(
                Wrappers.<KeyRotationLog>lambdaQuery()
                        .eq(KeyRotationLog::getTargetType, "PLATFORM_CREDENTIAL")
                        .eq(KeyRotationLog::getTargetId, credentialId)
                        .orderByDesc(KeyRotationLog::getId)
        );
        return logs.stream().map(KeyRotationLogResponse::from).toList();
    }

    /**
     * 管理员视图：查询全平台凭证列表，支持按状态和用户过滤。
     */
    public List<PlatformCredential> adminList(String status, Long userId) {
        var query = Wrappers.<PlatformCredential>lambdaQuery();
        if (status != null && !status.isBlank()) query.eq(PlatformCredential::getStatus, status);
        if (userId != null) query.eq(PlatformCredential::getUserId, userId);
        query.orderByDesc(PlatformCredential::getId);
        return platformCredentialMapper.selectList(query);
    }

    /**
     * 更新用户名下个人凭证（优先 {@code PERSONAL}）的月度配额、策略与展示名称。
     * <p>仅写入非 {@code null} 的数值/策略；名称仅在非空串时更新。若无任何凭证则不做操作。</p>
     *
     * @param userId              用户 ID
     * @param monthlyTokenQuota   月度 Token 上限，{@code 0} 表示不限制
     * @param alertThresholdPct   告警阈值百分比
     * @param overQuotaStrategy   超配额策略
     * @param credentialName      凭证展示名称
     * @return 更新后的凭证；用户无任何凭证时返回 {@code null}
     */
    public PlatformCredential patchPersonalCredentialForUser(Long userId, Long monthlyTokenQuota,
                                                             Integer alertThresholdPct,
                                                             String overQuotaStrategy,
                                                             String credentialName) {
        boolean touchQuota = monthlyTokenQuota != null || alertThresholdPct != null || overQuotaStrategy != null;
        boolean touchName = credentialName != null && !credentialName.isBlank();
        if (!touchQuota && !touchName) {
            return null;
        }
        List<PlatformCredential> list = listByUserId(userId);
        if (list.isEmpty()) {
            return null;
        }
        PlatformCredential c = list.stream()
                .filter(x -> "PERSONAL".equals(x.getCredentialType()))
                .findFirst()
                .orElse(list.get(0));
        if (monthlyTokenQuota != null) {
            c.setMonthlyTokenQuota(monthlyTokenQuota);
        }
        if (alertThresholdPct != null) {
            c.setAlertThresholdPct(alertThresholdPct);
        }
        if (overQuotaStrategy != null && !overQuotaStrategy.isBlank()) {
            c.setOverQuotaStrategy(overQuotaStrategy.trim());
        }
        if (touchName) {
            c.setName(credentialName.trim());
        }
        platformCredentialMapper.updateById(c);
        return c;
    }

    // ------------------------------------------------------------------
    // private helpers
    // ------------------------------------------------------------------

    private String sha256Hex(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
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
