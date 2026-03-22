package com.aiplatform.agent.gateway.service;

import com.aiplatform.agent.gateway.mapper.PlatformCredentialRefMapper;
import com.aiplatform.agent.gateway.mapper.ProjectRefMapper;
import org.springframework.stereotype.Service;

/**
 * 配额扣减服务。
 *
 * <p>在网关调用上游成功后，原子递增个人月度用量和项目月度用量（双池模型）。
 * 使用 SQL 原子操作 {@code SET used = used + N} 保证并发安全。</p>
 */
@Service
public class QuotaDeductionService {

    private final PlatformCredentialRefMapper credentialMapper;
    private final ProjectRefMapper projectMapper;

    public QuotaDeductionService(PlatformCredentialRefMapper credentialMapper,
                                  ProjectRefMapper projectMapper) {
        this.credentialMapper = credentialMapper;
        this.projectMapper = projectMapper;
    }

    /**
     * 扣减配额。
     *
     * @param credentialId 凭证 ID
     * @param projectId    项目 ID，可为 null
     * @param totalTokens  本次消耗的总 Token 数
     */
    public void deduct(Long credentialId, Long projectId, long totalTokens) {
        if (totalTokens <= 0) {
            return;
        }
        credentialMapper.incrementUsedTokens(credentialId, totalTokens);
        if (projectId != null) {
            projectMapper.incrementUsedTokens(projectId, totalTokens);
        }
    }
}
