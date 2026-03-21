package com.aiplatform.backend.service;

import com.aiplatform.backend.common.exception.AiProviderNotFoundException;
import com.aiplatform.backend.dto.CreateAiProviderRequest;
import com.aiplatform.backend.entity.AiProvider;
import com.aiplatform.backend.mapper.AiProviderMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * AI 供应商业务服务。
 * <p>提供 AI 供应商的创建、查询等操作。</p>
 */
@Service
public class AiProviderService {

    private final AiProviderMapper aiProviderMapper;

    /**
     * 构造方法，注入供应商数据访问层。
     *
     * @param aiProviderMapper 供应商 Mapper
     */
    public AiProviderService(AiProviderMapper aiProviderMapper) {
        this.aiProviderMapper = aiProviderMapper;
    }

    /**
     * 创建新的 AI 供应商。
     * <p>新供应商默认状态为 ACTIVE。</p>
     *
     * @param request 创建供应商请求参数
     * @return 新创建的供应商实体
     */
    public AiProvider create(CreateAiProviderRequest request) {
        AiProvider provider = new AiProvider();
        provider.setCode(request.code());
        provider.setName(request.name());
        provider.setProviderType(request.providerType());
        provider.setBaseUrl(request.baseUrl());
        provider.setStatus("ACTIVE");
        aiProviderMapper.insert(provider);
        return provider;
    }

    /**
     * 查询全部供应商列表，按ID升序排列。
     *
     * @return 供应商列表
     */
    public List<AiProvider> list() {
        return aiProviderMapper.selectList(Wrappers.<AiProvider>lambdaQuery().orderByAsc(AiProvider::getId));
    }

    /**
     * 根据ID查询供应商，若不存在则抛出异常。
     *
     * @param providerId 供应商ID
     * @return 供应商实体
     * @throws AiProviderNotFoundException 当供应商不存在时抛出
     */
    public AiProvider getByIdOrThrow(Long providerId) {
        AiProvider provider = aiProviderMapper.selectById(providerId);
        if (provider == null) {
            throw new AiProviderNotFoundException(providerId);
        }
        return provider;
    }
}
