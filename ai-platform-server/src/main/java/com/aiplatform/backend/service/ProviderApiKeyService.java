package com.aiplatform.backend.service;

import com.aiplatform.backend.common.exception.ProviderApiKeyNotFoundException;
import com.aiplatform.backend.dto.CreateProviderApiKeyRequest;
import com.aiplatform.backend.entity.ProviderApiKey;
import com.aiplatform.backend.mapper.ProviderApiKeyMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.List;

/**
 * 上游 API 密钥业务服务。
 * <p>处理 API 密钥的创建（含加密和前缀提取）、查询等操作，支持按供应商筛选。</p>
 */
@Service
public class ProviderApiKeyService {

    private final ProviderApiKeyMapper providerApiKeyMapper;

    /**
     * 构造方法，注入密钥数据访问层。
     *
     * @param providerApiKeyMapper 密钥 Mapper
     */
    public ProviderApiKeyService(ProviderApiKeyMapper providerApiKeyMapper) {
        this.providerApiKeyMapper = providerApiKeyMapper;
    }

    /**
     * 创建新的上游 API 密钥。
     * <p>将原始密钥进行 Base64 加密存储，并提取前12位字符作为安全展示前缀。
     * 新密钥的本月已用 Token 初始为0，默认状态为 ACTIVE。</p>
     *
     * @param request 创建密钥请求参数
     * @return 新创建的密钥实体
     */
    public ProviderApiKey create(CreateProviderApiKeyRequest request) {
        ProviderApiKey key = new ProviderApiKey();
        key.setProviderId(request.providerId());
        key.setLabel(request.label());

        String rawKey = request.apiKey();
        String prefix = rawKey.length() > 12 ? rawKey.substring(0, 12) + "..." : rawKey;
        key.setKeyPrefix(prefix);
        key.setApiKeyEncrypted(Base64.getEncoder().encodeToString(rawKey.getBytes()));

        key.setModelsAllowed(request.modelsAllowed());
        key.setMonthlyQuotaTokens(request.monthlyQuotaTokens());
        key.setUsedTokensMonth(0L);
        key.setRateLimitRpm(request.rateLimitRpm());
        key.setRateLimitTpm(request.rateLimitTpm());
        key.setProxyEndpoint(request.proxyEndpoint());
        key.setStatus("ACTIVE");
        providerApiKeyMapper.insert(key);
        return key;
    }

    /**
     * 查询全部密钥列表，按ID升序排列。
     *
     * @return 密钥列表
     */
    public List<ProviderApiKey> list() {
        return providerApiKeyMapper.selectList(Wrappers.<ProviderApiKey>lambdaQuery().orderByAsc(ProviderApiKey::getId));
    }

    /**
     * 根据供应商ID查询该供应商下的密钥列表，按ID升序排列。
     *
     * @param providerId 供应商ID
     * @return 该供应商下的密钥列表
     */
    public List<ProviderApiKey> listByProviderId(Long providerId) {
        return providerApiKeyMapper.selectList(
                Wrappers.<ProviderApiKey>lambdaQuery()
                        .eq(ProviderApiKey::getProviderId, providerId)
                        .orderByAsc(ProviderApiKey::getId)
        );
    }

    /**
     * 根据ID查询密钥，若不存在则抛出异常。
     *
     * @param keyId 密钥ID
     * @return 密钥实体
     * @throws ProviderApiKeyNotFoundException 当密钥不存在时抛出
     */
    public ProviderApiKey getByIdOrThrow(Long keyId) {
        ProviderApiKey key = providerApiKeyMapper.selectById(keyId);
        if (key == null) throw new ProviderApiKeyNotFoundException(keyId);
        return key;
    }

    /** 编辑Key配置（配额、限速等）。 */
    public ProviderApiKey update(Long id, CreateProviderApiKeyRequest request) {
        ProviderApiKey key = getByIdOrThrow(id);
        if (request.label() != null) key.setLabel(request.label());
        if (request.modelsAllowed() != null) key.setModelsAllowed(request.modelsAllowed());
        if (request.monthlyQuotaTokens() != null) key.setMonthlyQuotaTokens(request.monthlyQuotaTokens());
        if (request.rateLimitRpm() != null) key.setRateLimitRpm(request.rateLimitRpm());
        if (request.rateLimitTpm() != null) key.setRateLimitTpm(request.rateLimitTpm());
        if (request.proxyEndpoint() != null) key.setProxyEndpoint(request.proxyEndpoint());
        providerApiKeyMapper.updateById(key);
        return key;
    }

    /** 吊销 Key（status → REVOKED）。 */
    public ProviderApiKey revoke(Long id) {
        ProviderApiKey key = getByIdOrThrow(id);
        key.setStatus("REVOKED");
        providerApiKeyMapper.updateById(key);
        return key;
    }
}
