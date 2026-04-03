package com.aiplatform.backend.service;

import com.aiplatform.backend.common.exception.BizErrorCode;
import com.aiplatform.backend.common.exception.BusinessException;
import com.aiplatform.backend.common.exception.ForbiddenException;
import com.aiplatform.backend.common.exception.PlatformCredentialNotFoundException;
import com.aiplatform.backend.common.exception.UnauthorizedException;
import com.aiplatform.backend.dto.CreatePlatformCredentialRequest;
import com.aiplatform.backend.dto.CreatePlatformCredentialResponse;
import com.aiplatform.backend.dto.KeyRotationLogResponse;
import com.aiplatform.backend.dto.PlatformCredentialResponse;
import com.aiplatform.backend.dto.me.MeCredentialResponse;
import com.aiplatform.backend.dto.me.MeProjectOptionResponse;
import com.aiplatform.backend.entity.ActivityLog;
import com.aiplatform.backend.entity.KeyRotationLog;
import com.aiplatform.backend.entity.PlatformCredential;
import com.aiplatform.backend.entity.Project;
import com.aiplatform.backend.entity.ProjectMember;
import com.aiplatform.backend.mapper.ActivityLogMapper;
import com.aiplatform.backend.mapper.KeyRotationLogMapper;
import com.aiplatform.backend.mapper.PlatformCredentialMapper;
import com.aiplatform.backend.mapper.ProjectMapper;
import com.aiplatform.backend.mapper.ProjectMemberMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

/**
 * 平台凭证业务服务，负责凭证创建、校验、轮换和状态管理。
 */
@Service
public class PlatformCredentialService {

    private static final String STATUS_ACTIVE = "ACTIVE";
    private static final String STATUS_DISABLED = "DISABLED";
    private static final String STATUS_REVOKED = "REVOKED";
    private static final String STATUS_EXPIRED = "EXPIRED";
    private static final String TYPE_PERSONAL = "PERSONAL";
    private static final String ROTATION_TARGET_TYPE = "PLATFORM_CREDENTIAL";
    private static final String ROTATION_TYPE_MANUAL = "MANUAL";
    private static final String ROTATION_TYPE_RENEW = "RENEW";
    private static final String ROTATION_RESULT_SUCCESS = "SUCCESS";
    private static final String DEFAULT_CREDENTIAL_NAME = "Personal Credential";
    private static final String DEFAULT_ACTIVITY_ACTOR_NAME = "System";
    private static final String ACTIVITY_TARGET_TYPE_PLATFORM_CREDENTIAL = "platform_credential";
    private static final String ACTIVITY_TARGET_TYPE_PROJECT = "project";
    private static final String ACTIVITY_ACTION_CREDENTIAL_ROTATE = "credential.rotate";
    private static final String ACTIVITY_ACTION_CREDENTIAL_RENEW = "credential.renew";
    private static final String ACTIVITY_ACTION_CREDENTIAL_TEST = "credential.test";
    private static final String ACTIVITY_ACTION_CURRENT_PROJECT_UPDATE = "credential.current_project.update";
    private static final long DEFAULT_MONTHLY_TOKEN_QUOTA = 200_000L;
    private static final int DEFAULT_ALERT_THRESHOLD_PCT = 80;
    private static final String DEFAULT_OVER_QUOTA_STRATEGY = "BLOCK";

    private final PlatformCredentialMapper platformCredentialMapper;
    private final KeyRotationLogMapper keyRotationLogMapper;
    private final ProjectMemberMapper projectMemberMapper;
    private final ProjectMapper projectMapper;
    private final ActivityLogMapper activityLogMapper;

    public PlatformCredentialService(PlatformCredentialMapper platformCredentialMapper,
                                     KeyRotationLogMapper keyRotationLogMapper,
                                     ProjectMemberMapper projectMemberMapper,
                                     ProjectMapper projectMapper,
                                     ActivityLogMapper activityLogMapper) {
        this.platformCredentialMapper = platformCredentialMapper;
        this.keyRotationLogMapper = keyRotationLogMapper;
        this.projectMemberMapper = projectMemberMapper;
        this.projectMapper = projectMapper;
        this.activityLogMapper = activityLogMapper;
    }

    /**
     * 创建平台凭证，并返回仅展示一次的明文密钥。
     */
    @Transactional(rollbackFor = Throwable.class)
    public CreatePlatformCredentialResponse create(CreatePlatformCredentialRequest request) {
        String rawKey = newRawPlatformKey(request.userId());

        PlatformCredential credential = new PlatformCredential();
        credential.setUserId(request.userId());
        credential.setCredentialType(request.credentialType() != null ? request.credentialType() : TYPE_PERSONAL);
        credential.setKeyHash(sha256Hex(rawKey));
        credential.setKeyPrefix(rawKey.substring(0, Math.min(12, rawKey.length())));
        credential.setName(request.name() != null ? request.name() : DEFAULT_CREDENTIAL_NAME);
        applyDefaultQuotaSettings(credential, request);
        credential.setLastQuotaResetAt(LocalDateTime.now());
        credential.setStatus(STATUS_ACTIVE);

        platformCredentialMapper.insert(credential);
        return new CreatePlatformCredentialResponse(rawKey, PlatformCredentialResponse.from(credential));
    }

    /**
     * 按用户查询凭证列表。
     */
    public List<PlatformCredential> listByUserId(Long userId) {
        return platformCredentialMapper.selectList(
                Wrappers.<PlatformCredential>lambdaQuery()
                        .eq(PlatformCredential::getUserId, userId)
                        .orderByAsc(PlatformCredential::getId)
        );
    }

    /**
     * 按用户查询唯一凭证，不存在时返回 null。
     */
    public PlatformCredential getByUserId(Long userId) {
        return platformCredentialMapper.selectOne(
                Wrappers.<PlatformCredential>lambdaQuery()
                        .eq(PlatformCredential::getUserId, userId)
        );
    }

    /**
     * 按用户查询唯一凭证，不存在则抛出异常。
     */
    public PlatformCredential getByUserIdOrThrow(Long userId) {
        PlatformCredential credential = getByUserId(userId);
        if (credential == null) {
            throw new BusinessException(
                    404,
                    BizErrorCode.PLATFORM_CREDENTIAL_NOT_FOUND,
                    "当前用户尚未创建平台凭证");
        }
        return credential;
    }

    /**
     * 校验平台密钥，并返回可转发给 Agent 的 Authorization 请求头。
     */
    public String authorizationHeaderForValidatedPlatformKey(Long userId, String rawOrBearerKey) {
        String raw = normalizePlatformKey(rawOrBearerKey);
        PlatformCredential credential = getByUserId(userId);
        if (credential == null) {
            throw new UnauthorizedException("未找到平台凭证，请先在平台创建个人凭证");
        }
        if (!STATUS_ACTIVE.equals(credential.getStatus())) {
            throw new UnauthorizedException("平台凭证不可用");
        }
        if (credential.getExpiresAt() != null && credential.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new UnauthorizedException("平台凭证已过期");
        }
        if (!sha256Hex(raw).equalsIgnoreCase(credential.getKeyHash())) {
            throw new UnauthorizedException("X-Platform-Key 与当前用户凭证不匹配");
        }
        return "Bearer " + raw;
    }

    /**
     * 吊销凭证。
     */
    @Transactional(rollbackFor = Throwable.class)
    public void revoke(Long id, String reason) {
        PlatformCredential credential = getByIdOrThrow(id);
        markRevoked(credential, reason);
        platformCredentialMapper.updateById(credential);
    }

    /**
     * 根据 ID 查询凭证，不存在则抛异常。
     */
    public PlatformCredential getByIdOrThrow(Long id) {
        PlatformCredential credential = platformCredentialMapper.selectById(id);
        if (credential == null) {
            throw new PlatformCredentialNotFoundException(id);
        }
        return credential;
    }

    /**
     * 批量吊销某用户名下所有 ACTIVE 凭证。
     */
    @Transactional(rollbackFor = Throwable.class)
    public void revokeAllByUserId(Long userId, String reason) {
        List<PlatformCredential> activeCredentials = platformCredentialMapper.selectList(
                Wrappers.<PlatformCredential>lambdaQuery()
                        .eq(PlatformCredential::getUserId, userId)
                        .eq(PlatformCredential::getStatus, STATUS_ACTIVE)
        );
        for (PlatformCredential credential : activeCredentials) {
            markRevoked(credential, reason);
            platformCredentialMapper.updateById(credential);
        }
    }

    /**
     * 续期凭证。
     */
    @Transactional(rollbackFor = Throwable.class)
    public PlatformCredential renew(Long id, int renewDays) {
        PlatformCredential credential = getByIdOrThrow(id);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime base = credential.getExpiresAt() != null && credential.getExpiresAt().isAfter(now)
                ? credential.getExpiresAt()
                : now;
        LocalDateTime newExpiresAt = base.plusDays(renewDays);
        credential.setExpiresAt(newExpiresAt);
        platformCredentialMapper.updateById(credential);

        recordRotationLog(credential, ROTATION_TYPE_RENEW);
        ActivityLog renewLog = new ActivityLog();
        renewLog.setProjectId(credential.getBoundProjectId());
        renewLog.setUserId(credential.getUserId());
        renewLog.setActorName(DEFAULT_ACTIVITY_ACTOR_NAME);
        renewLog.setActionType(ACTIVITY_ACTION_CREDENTIAL_RENEW);
        renewLog.setSummary("续签个人凭证，有效期延长 " + renewDays + " 天");
        renewLog.setTargetType(ACTIVITY_TARGET_TYPE_PLATFORM_CREDENTIAL);
        renewLog.setTargetId(credential.getId());
        renewLog.setTargetName(credential.getName());
        renewLog.setOccurredAt(LocalDateTime.now());
        activityLogMapper.insert(renewLog);
        return credential;
    }

    /**
     * 轮换凭证密钥。
     */
    @Transactional(rollbackFor = Throwable.class)
    public CreatePlatformCredentialResponse rotate(Long id, int gracePeriodHours) {
        PlatformCredential credential = getByIdOrThrow(id);
        recordRotationLog(credential, ROTATION_TYPE_MANUAL);

        String rawKey = newRawPlatformKey(credential.getUserId());
        credential.setKeyHash(sha256Hex(rawKey));
        credential.setKeyPrefix(rawKey.substring(0, Math.min(12, rawKey.length())));
        credential.setStatus(STATUS_ACTIVE);
        platformCredentialMapper.updateById(credential);

        ActivityLog rotateLog = new ActivityLog();
        rotateLog.setProjectId(credential.getBoundProjectId());
        rotateLog.setUserId(credential.getUserId());
        rotateLog.setActorName(DEFAULT_ACTIVITY_ACTOR_NAME);
        rotateLog.setActionType(ACTIVITY_ACTION_CREDENTIAL_ROTATE);
        rotateLog.setSummary("轮换个人凭证，宽限期 " + gracePeriodHours + " 小时");
        rotateLog.setTargetType(ACTIVITY_TARGET_TYPE_PLATFORM_CREDENTIAL);
        rotateLog.setTargetId(credential.getId());
        rotateLog.setTargetName(credential.getName());
        rotateLog.setOccurredAt(LocalDateTime.now());
        activityLogMapper.insert(rotateLog);

        return new CreatePlatformCredentialResponse(rawKey, PlatformCredentialResponse.from(credential));
    }

    /**
     * 按用户轮换个人凭证。
     */
    @Transactional(rollbackFor = Throwable.class)
    public CreatePlatformCredentialResponse rotateByUserId(Long userId, int gracePeriodHours) {
        PlatformCredential credential = getByUserIdOrThrow(userId);
        return rotate(credential.getId(), gracePeriodHours);
    }

    /**
     * 按用户续签个人凭证。
     */
    @Transactional(rollbackFor = Throwable.class)
    public PlatformCredential renewByUserId(Long userId, int renewDays) {
        PlatformCredential credential = getByUserIdOrThrow(userId);
        return renew(credential.getId(), renewDays);
    }

    /**
     * 记录凭证连通性测试活动日志。
     */
    @Transactional(rollbackFor = Throwable.class)
    public void recordCredentialTestActivity(Long userId, Long clientAppId) {
        PlatformCredential credential = getByUserIdOrThrow(userId);
        String summary = clientAppId == null
                ? "测试凭证连通性"
                : "测试凭证连通性，客户端 ID=" + clientAppId;
        ActivityLog testLog = new ActivityLog();
        testLog.setProjectId(credential.getBoundProjectId());
        testLog.setUserId(userId);
        testLog.setActorName(DEFAULT_ACTIVITY_ACTOR_NAME);
        testLog.setActionType(ACTIVITY_ACTION_CREDENTIAL_TEST);
        testLog.setSummary(summary);
        testLog.setTargetType(ACTIVITY_TARGET_TYPE_PLATFORM_CREDENTIAL);
        testLog.setTargetId(credential.getId());
        testLog.setTargetName(credential.getName());
        testLog.setOccurredAt(LocalDateTime.now());
        activityLogMapper.insert(testLog);
    }

    /**
     * 查询凭证轮换日志。
     */
    public List<KeyRotationLogResponse> listRotationLogs(Long credentialId) {
        List<KeyRotationLog> logs = keyRotationLogMapper.selectList(
                Wrappers.<KeyRotationLog>lambdaQuery()
                        .eq(KeyRotationLog::getTargetType, ROTATION_TARGET_TYPE)
                        .eq(KeyRotationLog::getTargetId, credentialId)
                        .orderByDesc(KeyRotationLog::getId)
        );
        return logs.stream().map(KeyRotationLogResponse::from).toList();
    }

    /**
     * 管理员视角查询全平台凭证列表。
     */
    public List<PlatformCredential> adminList(String status, Long userId) {
        var query = Wrappers.<PlatformCredential>lambdaQuery();
        if (status != null && !status.isBlank()) {
            query.eq(PlatformCredential::getStatus, status);
        }
        if (userId != null) {
            query.eq(PlatformCredential::getUserId, userId);
        }
        query.orderByDesc(PlatformCredential::getId);
        return platformCredentialMapper.selectList(query);
    }

    /**
     * 更新凭证绑定的当前工作项目。
     */
    @Transactional(rollbackFor = Throwable.class)
    public PlatformCredential updateBoundProject(Long credentialId, Long currentUserId, Long projectId) {
        PlatformCredential credential = requireOwnedCredential(credentialId, currentUserId);
        validateProjectMembership(currentUserId, projectId);
        credential.setBoundProjectId(projectId);
        platformCredentialMapper.updateById(credential);

        String summary = projectId == null
                ? "清除当前工作项目"
                : "切换当前工作项目为 ID=" + projectId;
        ActivityLog projectLog = new ActivityLog();
        projectLog.setProjectId(projectId);
        projectLog.setUserId(currentUserId);
        projectLog.setActorName(DEFAULT_ACTIVITY_ACTOR_NAME);
        projectLog.setActionType(ACTIVITY_ACTION_CURRENT_PROJECT_UPDATE);
        projectLog.setSummary(summary);
        projectLog.setTargetType(ACTIVITY_TARGET_TYPE_PROJECT);
        projectLog.setTargetId(projectId);
        projectLog.setTargetName(projectId == null ? null : "project-" + projectId);
        projectLog.setOccurredAt(LocalDateTime.now());
        activityLogMapper.insert(projectLog);

        return credential;
    }

    /**
     * 获取当前用户个人凭证聚合信息。
     */
    public MeCredentialResponse getMeCredential(Long userId) {
        PlatformCredential credential = getByUserIdOrThrow(userId);

        List<MeProjectOptionResponse> accessibleProjects = listAccessibleProjects(userId);
        MeProjectOptionResponse defaultProject = credential.getBoundProjectId() == null
                ? null
                : findProjectOption(accessibleProjects, credential.getBoundProjectId());

        return new MeCredentialResponse(
                credential.getId(),
                credential.getUserId(),
                credential.getKeyPrefix(),
                credential.getCredentialType(),
                credential.getStatus(),
                credential.getExpiresAt(),
                credential.getBoundProjectId(),
                defaultProject,
                accessibleProjects
        );
    }

    /**
     * 按当前登录用户更新默认项目（即当前工作项目）。
     */
    @Transactional(rollbackFor = Throwable.class)
    public PlatformCredential updateCurrentProjectByUserId(Long currentUserId, Long projectId) {
        PlatformCredential credential = getByUserIdOrThrow(currentUserId);
        validateProjectMembership(currentUserId, projectId);
        credential.setBoundProjectId(projectId);
        platformCredentialMapper.updateById(credential);

        String summary = projectId == null
                ? "清除当前工作项目"
                : "切换当前工作项目为 ID=" + projectId;
        ActivityLog projectLog = new ActivityLog();
        projectLog.setProjectId(projectId);
        projectLog.setUserId(currentUserId);
        projectLog.setActorName(DEFAULT_ACTIVITY_ACTOR_NAME);
        projectLog.setActionType(ACTIVITY_ACTION_CURRENT_PROJECT_UPDATE);
        projectLog.setSummary(summary);
        projectLog.setTargetType(ACTIVITY_TARGET_TYPE_PROJECT);
        projectLog.setTargetId(projectId);
        projectLog.setTargetName(projectId == null ? null : "project-" + projectId);
        projectLog.setOccurredAt(LocalDateTime.now());
        activityLogMapper.insert(projectLog);

        return credential;
    }

    /**
     * 更新用户个人凭证配额和展示名称。
     */
    @Transactional(rollbackFor = Throwable.class)
    public PlatformCredential patchPersonalCredentialForUser(PlatformCredentialPatchCommand command) {
        Long userId = command.userId();
        Long monthlyTokenQuota = command.monthlyTokenQuota();
        Integer alertThresholdPct = command.alertThresholdPct();
        String overQuotaStrategy = command.overQuotaStrategy();
        String credentialName = command.credentialName();

        boolean touchQuota = monthlyTokenQuota != null || alertThresholdPct != null || overQuotaStrategy != null;
        boolean touchName = credentialName != null && !credentialName.isBlank();
        if (!touchQuota && !touchName) {
            return null;
        }

        List<PlatformCredential> credentials = listByUserId(userId);
        if (credentials.isEmpty()) {
            return null;
        }

        PlatformCredential credential = selectPersonalOrFirstCredential(credentials);
        if (monthlyTokenQuota != null) {
            credential.setMonthlyTokenQuota(monthlyTokenQuota);
        }
        if (alertThresholdPct != null) {
            credential.setAlertThresholdPct(alertThresholdPct);
        }
        if (overQuotaStrategy != null && !overQuotaStrategy.isBlank()) {
            credential.setOverQuotaStrategy(overQuotaStrategy.trim());
        }
        if (touchName) {
            credential.setName(credentialName.trim());
        }
        platformCredentialMapper.updateById(credential);
        return credential;
    }

    /**
     * 启用或停用用户个人凭证的 AI 调用能力。
     */
    @Transactional(rollbackFor = Throwable.class)
    public PlatformCredential setAiAccessEnabled(Long userId, boolean enabled) {
        PlatformCredential credential = getByUserId(userId);
        if (credential == null) {
            throw new BusinessException(
                    400,
                    BizErrorCode.VALIDATION_FAILED,
                    "该用户尚未创建平台凭证，无法切换 AI 权限");
        }

        if (enabled) {
            if (STATUS_REVOKED.equals(credential.getStatus())) {
                throw new BusinessException(400, BizErrorCode.VALIDATION_FAILED, "凭证已吊销，无法启用");
            }
            if (STATUS_EXPIRED.equals(credential.getStatus())) {
                throw new BusinessException(400, BizErrorCode.VALIDATION_FAILED, "凭证已过期，请先续期");
            }
            credential.setStatus(STATUS_ACTIVE);
        } else if (!STATUS_REVOKED.equals(credential.getStatus())) {
            credential.setStatus(STATUS_DISABLED);
        }
        platformCredentialMapper.updateById(credential);
        return credential;
    }

    /**
     * 清零个人凭证的当月已用 Token。
     */
    @Transactional(rollbackFor = Throwable.class)
    public void resetUsedTokensThisMonthForUser(Long userId) {
        PlatformCredential credential = getByUserId(userId);
        if (credential == null) {
            return;
        }
        credential.setUsedTokensThisMonth(0L);
        platformCredentialMapper.updateById(credential);
    }

    private void applyDefaultQuotaSettings(PlatformCredential credential, CreatePlatformCredentialRequest request) {
        credential.setMonthlyTokenQuota(
                request.monthlyTokenQuota() != null ? request.monthlyTokenQuota() : DEFAULT_MONTHLY_TOKEN_QUOTA);
        credential.setUsedTokensThisMonth(0L);
        credential.setAlertThresholdPct(
                request.alertThresholdPct() != null ? request.alertThresholdPct() : DEFAULT_ALERT_THRESHOLD_PCT);
        credential.setOverQuotaStrategy(
                request.overQuotaStrategy() != null ? request.overQuotaStrategy() : DEFAULT_OVER_QUOTA_STRATEGY);
    }

    private PlatformCredential requireOwnedCredential(Long credentialId, Long currentUserId) {
        PlatformCredential credential = getByIdOrThrow(credentialId);
        if (!credential.getUserId().equals(currentUserId)) {
            throw new ForbiddenException("只能修改自己的凭证");
        }
        return credential;
    }

    private void validateProjectMembership(Long userId, Long projectId) {
        if (projectId == null) {
            return;
        }
        long memberRows = projectMemberMapper.selectCount(
                Wrappers.<ProjectMember>lambdaQuery()
                        .eq(ProjectMember::getProjectId, projectId)
                        .eq(ProjectMember::getUserId, userId)
        );
        if (memberRows == 0) {
            throw new ForbiddenException("只能绑定已加入的项目");
        }
    }

    private List<MeProjectOptionResponse> listAccessibleProjects(Long userId) {
        List<ProjectMember> memberships = projectMemberMapper.selectList(
                Wrappers.<ProjectMember>lambdaQuery()
                        .eq(ProjectMember::getUserId, userId)
                        .orderByAsc(ProjectMember::getProjectId)
        );
        if (memberships.isEmpty()) {
            return List.of();
        }

        List<Long> projectIds = memberships.stream()
                .map(ProjectMember::getProjectId)
                .distinct()
                .toList();
        List<Project> projects = projectMapper.selectList(
                Wrappers.<Project>lambdaQuery()
                        .in(Project::getId, projectIds)
                        .orderByAsc(Project::getId)
        );
        return projects.stream()
                .map(project -> new MeProjectOptionResponse(project.getId(), project.getName(), project.getCode()))
                .toList();
    }

    private MeProjectOptionResponse findProjectOption(List<MeProjectOptionResponse> projects, Long projectId) {
        if (projectId == null) {
            return null;
        }
        return projects.stream()
                .filter(project -> project.id().equals(projectId))
                .findFirst()
                .orElse(new MeProjectOptionResponse(projectId, null, null));
    }

    private void markRevoked(PlatformCredential credential, String reason) {
        credential.setStatus(STATUS_REVOKED);
        credential.setRevokedAt(LocalDateTime.now());
        credential.setRevokeReason(reason);
    }

    private void recordRotationLog(PlatformCredential credential, String rotationType) {
        KeyRotationLog log = new KeyRotationLog();
        log.setTargetType(ROTATION_TARGET_TYPE);
        log.setTargetId(credential.getId());
        log.setOldKeyPrefix(credential.getKeyPrefix());
        log.setRotationType(rotationType);
        log.setOperatedBy(credential.getUserId());
        log.setResult(ROTATION_RESULT_SUCCESS);
        keyRotationLogMapper.insert(log);
    }

    private PlatformCredential selectPersonalOrFirstCredential(List<PlatformCredential> credentials) {
        return credentials.stream()
                .filter(credential -> TYPE_PERSONAL.equals(credential.getCredentialType()))
                .findFirst()
                .orElse(credentials.get(0));
    }

    private static String normalizePlatformKey(String rawOrBearerKey) {
        if (rawOrBearerKey == null || rawOrBearerKey.isBlank()) {
            throw new UnauthorizedException("缺少 X-Platform-Key 请求头");
        }
        String token = rawOrBearerKey.trim();
        if (token.regionMatches(true, 0, "Bearer ", 0, 7)) {
            token = token.substring(7).trim();
        }
        if (token.isEmpty()) {
            throw new UnauthorizedException("X-Platform-Key 不能为空");
        }
        return token;
    }

    private String newRawPlatformKey(Long userId) {
        return "plt_" + userId + "_"
                + UUID.randomUUID().toString().replace("-", "").substring(0, 16)
                + "_" + checksumChar(userId);
    }

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

    /**
     * 更新用户个人凭证补丁命令。
     */
    public record PlatformCredentialPatchCommand(
            Long userId,
            Long monthlyTokenQuota,
            Integer alertThresholdPct,
            String overQuotaStrategy,
            String credentialName
    ) {
    }
}
