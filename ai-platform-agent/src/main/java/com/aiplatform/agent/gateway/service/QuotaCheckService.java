package com.aiplatform.agent.gateway.service;

import com.aiplatform.agent.gateway.entity.PlatformCredentialRef;
import com.aiplatform.agent.gateway.entity.ProjectRef;
import com.aiplatform.agent.gateway.mapper.ProjectRefMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 配额校验服务。
 *
 * <p>在网关调用上游之前，执行双池 check 阶段：成员池 + 项目池。</p>
 */
@Service
public class QuotaCheckService {

    private static final Logger log = LoggerFactory.getLogger(QuotaCheckService.class);

    private final ProjectRefMapper projectMapper;

    public QuotaCheckService(ProjectRefMapper projectMapper) {
        this.projectMapper = projectMapper;
    }

    /**
     * 双池配额 check 阶段。
     */
    public QuotaCheckResult check(PlatformCredentialRef credential, Long projectId) {
        QuotaCheckResult memberResult = checkMemberPool(credential);
        QuotaCheckResult projectResult = projectId == null ? QuotaCheckResult.ok() : checkProjectPool(projectId);

        if (projectResult.code() == QuotaCheckCode.PROJECT_POOL_ALERT) {
            return projectResult;
        }
        if (memberResult.code() == QuotaCheckCode.MEMBER_POOL_ALERT) {
            return memberResult;
        }
        return QuotaCheckResult.ok();
    }

    private QuotaCheckResult checkMemberPool(PlatformCredentialRef credential) {
        Long quota = credential.getMonthlyTokenQuota();
        if (quota == null || quota <= 0) {
            return QuotaCheckResult.ok();
        }

        long used = credential.getUsedTokensThisMonth() == null ? 0L : credential.getUsedTokensThisMonth();
        if (used < quota) {
            return QuotaCheckResult.ok();
        }

        if ("ALLOW_WITH_ALERT".equals(credential.getOverQuotaStrategy())) {
            log.warn("成员池配额已超限（已用 {}/{}），策略 ALLOW_WITH_ALERT 放行。userId={}",
                    used, quota, credential.getUserId());
            return new QuotaCheckResult(QuotaCheckCode.MEMBER_POOL_ALERT, "成员池配额已超限，按告警策略放行");
        }

        throw new QuotaExceededException(QuotaCheckCode.MEMBER_POOL_INSUFFICIENT, "成员池不足");
    }

    private QuotaCheckResult checkProjectPool(Long projectId) {
        ProjectRef project = projectMapper.selectById(projectId);
        if (project == null) {
            throw new QuotaExceededException(QuotaCheckCode.PROJECT_POOL_INSUFFICIENT, "项目不存在或不可用");
        }

        Long quota = project.getMonthlyTokenQuota();
        if (quota == null || quota <= 0) {
            return QuotaCheckResult.ok();
        }

        long used = project.getUsedTokensThisMonth() == null ? 0L : project.getUsedTokensThisMonth();
        if (used < quota) {
            return QuotaCheckResult.ok();
        }

        if ("ALLOW_WITH_ALERT".equals(project.getOverQuotaStrategy())) {
            log.warn("项目池配额已超限（已用 {}/{}），策略 ALLOW_WITH_ALERT 放行。projectId={}",
                    used, quota, projectId);
            return new QuotaCheckResult(QuotaCheckCode.PROJECT_POOL_ALERT, "项目池配额已超限，按告警策略放行");
        }

        throw new QuotaExceededException(QuotaCheckCode.PROJECT_POOL_INSUFFICIENT, "项目池不足");
    }

    public record QuotaCheckResult(QuotaCheckCode code, String message) {
        public static QuotaCheckResult ok() {
            return new QuotaCheckResult(QuotaCheckCode.OK, "OK");
        }
    }

    public enum QuotaCheckCode {
        OK,
        MEMBER_POOL_ALERT,
        PROJECT_POOL_ALERT,
        MEMBER_POOL_INSUFFICIENT,
        PROJECT_POOL_INSUFFICIENT
    }

    /**
     * 配额超限异常。
     */
    public static class QuotaExceededException extends RuntimeException {

        private final QuotaCheckCode code;

        public QuotaExceededException(QuotaCheckCode code, String message) {
            super(message);
            this.code = code;
        }

        public QuotaCheckCode getCode() {
            return code;
        }
    }
}
