package com.aiplatform.agent.gateway.service;

import com.aiplatform.agent.gateway.entity.AiModelRef;
import com.aiplatform.agent.gateway.entity.AiProviderRef;
import com.aiplatform.agent.gateway.entity.ProviderApiKeyRef;
import com.aiplatform.agent.gateway.mapper.AiModelRefMapper;
import com.aiplatform.agent.gateway.mapper.AiProviderRefMapper;
import com.aiplatform.agent.gateway.mapper.ProviderApiKeyRefMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.stereotype.Service;

/**
 * 网关路由服务。
 *
 * <p>根据请求中指定的模型编码，查找对应的 AI 模型、上游供应商和可用 API 密钥，
 * 组装完整的路由信息供网关发起上游调用。若模型不存在、供应商不可用或无可用密钥，
 * 将分别抛出对应的异常。</p>
 */
@Service
public class GatewayRoutingService {

    /** AI 模型数据访问 Mapper */
    private final AiModelRefMapper modelMapper;

    /** AI 供应商数据访问 Mapper */
    private final AiProviderRefMapper providerMapper;

    /** 上游 API 密钥数据访问 Mapper */
    private final ProviderApiKeyRefMapper apiKeyMapper;

    /**
     * 构造网关路由服务。
     *
     * @param modelMapper    AI 模型数据访问 Mapper
     * @param providerMapper AI 供应商数据访问 Mapper
     * @param apiKeyMapper   上游 API 密钥数据访问 Mapper
     */
    public GatewayRoutingService(AiModelRefMapper modelMapper,
                                  AiProviderRefMapper providerMapper,
                                  ProviderApiKeyRefMapper apiKeyMapper) {
        this.modelMapper = modelMapper;
        this.providerMapper = providerMapper;
        this.apiKeyMapper = apiKeyMapper;
    }

    /**
     * 根据模型编码解析路由信息。
     *
     * <p>依次查找模型 → 供应商 → API 密钥，构建完整的路由结果。</p>
     *
     * @param modelCode 模型标识编码
     * @return 包含模型、供应商和 API 密钥的路由结果
     * @throws ModelNotFoundException       模型不存在或已停用时抛出
     * @throws ProviderNotAvailableException 供应商不可用或无可用 API 密钥时抛出
     */
    public RoutingResult resolve(String modelCode) {
        AiModelRef model = modelMapper.selectOne(
                Wrappers.<AiModelRef>lambdaQuery()
                        .eq(AiModelRef::getCode, modelCode)
                        .eq(AiModelRef::getStatus, "ACTIVE")
                        .last("LIMIT 1")
        );
        if (model == null) {
            throw new ModelNotFoundException("Model not found: " + modelCode);
        }

        AiProviderRef provider = providerMapper.selectOne(
                Wrappers.<AiProviderRef>lambdaQuery()
                        .eq(AiProviderRef::getId, model.getProviderId())
                        .eq(AiProviderRef::getStatus, "ACTIVE")
                        .last("LIMIT 1")
        );
        if (provider == null) {
            throw new ProviderNotAvailableException("Provider not available for model: " + modelCode);
        }

        ProviderApiKeyRef apiKey = apiKeyMapper.selectOne(
                Wrappers.<ProviderApiKeyRef>lambdaQuery()
                        .eq(ProviderApiKeyRef::getProviderId, provider.getId())
                        .eq(ProviderApiKeyRef::getStatus, "ACTIVE")
                        .last("LIMIT 1")
        );
        if (apiKey == null) {
            throw new ProviderNotAvailableException("No active API key for provider: " + provider.getCode());
        }

        return new RoutingResult(model, provider, apiKey);
    }

    /**
     * 路由解析结果。
     *
     * @param model    匹配到的 AI 模型
     * @param provider 模型所属的上游供应商
     * @param apiKey   供应商对应的可用 API 密钥
     */
    public record RoutingResult(AiModelRef model, AiProviderRef provider, ProviderApiKeyRef apiKey) {}

    /**
     * 模型未找到异常。
     *
     * <p>当请求的模型编码在系统中不存在或已停用时抛出。</p>
     */
    public static class ModelNotFoundException extends RuntimeException {
        /**
         * @param message 错误描述信息
         */
        public ModelNotFoundException(String message) { super(message); }
    }

    /**
     * 供应商不可用异常。
     *
     * <p>当模型对应的供应商不可用或该供应商无活跃的 API 密钥时抛出。</p>
     */
    public static class ProviderNotAvailableException extends RuntimeException {
        /**
         * @param message 错误描述信息
         */
        public ProviderNotAvailableException(String message) { super(message); }
    }
}
