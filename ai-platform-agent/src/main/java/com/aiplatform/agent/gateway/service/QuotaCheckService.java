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
 * <p>在网关调用上游之前，校验个人月度配额和项目月度配额（双池模型）。
 * 任一池超限则根据 overQuotaStrategy 决定是拒绝、告警还是降级。</p>
 */
@Service
public class QuotaCheckService {

    private static final Logger log = LoggerFactory.getLogger(QuotaCheckService.class);

    private final ProjectRefMapper projectMapper;

    public QuotaCheckService(ProjectRefMapper projectMapper) {
        this.projectMapper = projectMapper;
    }

    /**
     * 校验个人配额和项目配额。
     *
     * @param credential 当前调用方凭证（含配额信息）
     * @param projectId  项目 ID，可为 null
     * @throws QuotaExceededException 配额不足且策略为 BLOCK 时抛出
     */
    public void check(PlatformCredentialRef credential, Long projectId) {
        checkPersonalQuota(credential);
        if (projectId != null) {
            checkProjectQuota(projectId);
        }
    }

    private void checkPersonalQuota(PlatformCredentialRef credential) {
        Long quota = credential.getMonthlyTokenQuota();
        if (quota == null || quota <= 0) {
            return; // 0 或 null 表示不限制
        }
        Long used = credential.getUsedTokensThisMonth();
        if (used == null) {
            used = 0L;
        }
        if (used >= quota) {
            String strategy = credential.getOverQuotaStrategy();
            if ("ALLOW_WITH_ALERT".equals(strategy)) {
                log.warn("个人配额已超限（已用 {}/{}），策略为 ALLOW_WITH_ALERT，放行并告警。userId={}",
                        used, quota, credential.getUserId());
                return;
            }
            throw new QuotaExceededException(
                    "Personal monthly token quota exceeded (used: " + used + ", quota: " + quota + ")");
        }
    }

    private void checkProjectQuota(Long projectId) {
        ProjectRef project = projectMapper.selectById(projectId);
        if (project == null) {
            return; // 项目不存在则跳过检查
        }
        Long quota = project.getMonthlyTokenQuota();
        if (quota == null || quota <= 0) {
            return;
        }
        Long used = project.getUsedTokensThisMonth();
        if (used == null) {
            used = 0L;
        }
        if (used >= quota) {
            String strategy = project.getOverQuotaStrategy();
            if ("ALLOW_WITH_ALERT".equals(strategy)) {
                log.warn("项目配额已超限（已用 {}/{}），策略为 ALLOW_WITH_ALERT，放行并告警。projectId={}",
                        used, quota, projectId);
                return;
            }
            throw new QuotaExceededException(
                    "Project monthly token quota exceeded (used: " + used + ", quota: " + quota + ", project: " + project.getName() + ")");
        }
    }

    /**
     * 配额超限异常。
     */
    public static class QuotaExceededException extends RuntimeException {
        public QuotaExceededException(String message) {
            super(message);
        }
    }
}
