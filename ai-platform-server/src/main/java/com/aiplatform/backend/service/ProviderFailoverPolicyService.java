package com.aiplatform.backend.service;

import com.aiplatform.backend.common.exception.ProviderFailoverPolicyNotFoundException;
import com.aiplatform.backend.dto.CreateProviderFailoverPolicyRequest;
import com.aiplatform.backend.entity.ProviderFailoverPolicy;
import com.aiplatform.backend.mapper.ProviderFailoverPolicyMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 故障转移策略业务服务。
 * <p>提供故障转移策略的创建、查询等操作。</p>
 */
@Service
public class ProviderFailoverPolicyService {

    private final ProviderFailoverPolicyMapper providerFailoverPolicyMapper;

    /**
     * 构造方法，注入策略数据访问层。
     *
     * @param providerFailoverPolicyMapper 策略 Mapper
     */
    public ProviderFailoverPolicyService(ProviderFailoverPolicyMapper providerFailoverPolicyMapper) {
        this.providerFailoverPolicyMapper = providerFailoverPolicyMapper;
    }

    /**
     * 创建新的故障转移策略。
     * <p>新策略默认状态为 ACTIVE。</p>
     *
     * @param request 创建策略请求参数
     * @return 新创建的策略实体
     */
    public ProviderFailoverPolicy create(CreateProviderFailoverPolicyRequest request) {
        ProviderFailoverPolicy policy = new ProviderFailoverPolicy();
        policy.setName(request.name());
        policy.setPrimaryKeyId(request.primaryKeyId());
        policy.setFallbackKeyId(request.fallbackKeyId());
        policy.setTriggerCondition(request.triggerCondition());
        policy.setTriggerThreshold(request.triggerThreshold());
        policy.setAutoRecovery(request.autoRecovery());
        policy.setStatus("ACTIVE");
        providerFailoverPolicyMapper.insert(policy);
        return policy;
    }

    /**
     * 查询全部故障转移策略列表，按ID升序排列。
     *
     * @return 策略列表
     */
    public List<ProviderFailoverPolicy> list() {
        return providerFailoverPolicyMapper.selectList(
                Wrappers.<ProviderFailoverPolicy>lambdaQuery().orderByAsc(ProviderFailoverPolicy::getId)
        );
    }

    /**
     * 根据ID查询策略，若不存在则抛出异常。
     *
     * @param policyId 策略ID
     * @return 策略实体
     * @throws ProviderFailoverPolicyNotFoundException 当策略不存在时抛出
     */
    public ProviderFailoverPolicy getByIdOrThrow(Long policyId) {
        ProviderFailoverPolicy policy = providerFailoverPolicyMapper.selectById(policyId);
        if (policy == null) throw new ProviderFailoverPolicyNotFoundException(policyId);
        return policy;
    }

    /** 编辑故障转移策略。 */
    public ProviderFailoverPolicy update(Long id, CreateProviderFailoverPolicyRequest request) {
        ProviderFailoverPolicy policy = getByIdOrThrow(id);
        if (request.name() != null) policy.setName(request.name());
        if (request.primaryKeyId() != null) policy.setPrimaryKeyId(request.primaryKeyId());
        if (request.fallbackKeyId() != null) policy.setFallbackKeyId(request.fallbackKeyId());
        if (request.triggerCondition() != null) policy.setTriggerCondition(request.triggerCondition());
        if (request.triggerThreshold() != null) policy.setTriggerThreshold(request.triggerThreshold());
        if (request.autoRecovery() != null) policy.setAutoRecovery(request.autoRecovery());
        providerFailoverPolicyMapper.updateById(policy);
        return policy;
    }
}
