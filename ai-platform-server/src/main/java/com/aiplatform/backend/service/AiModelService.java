package com.aiplatform.backend.service;

import com.aiplatform.backend.common.exception.AiModelNotFoundException;
import com.aiplatform.backend.dto.CreateAiModelRequest;
import com.aiplatform.backend.entity.AiModel;
import com.aiplatform.backend.mapper.AiModelMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * AI 模型业务服务。
 * <p>提供 AI 模型的创建、查询等操作，支持按供应商筛选。</p>
 */
@Service
public class AiModelService {

    private final AiModelMapper aiModelMapper;

    /**
     * 构造方法，注入模型数据访问层。
     *
     * @param aiModelMapper 模型 Mapper
     */
    public AiModelService(AiModelMapper aiModelMapper) {
        this.aiModelMapper = aiModelMapper;
    }

    /**
     * 创建新的 AI 模型。
     * <p>新模型默认状态为 ACTIVE。</p>
     *
     * @param request 创建模型请求参数
     * @return 新创建的模型实体
     */
    public AiModel create(CreateAiModelRequest request) {
        AiModel model = new AiModel();
        model.setProviderId(request.providerId());
        model.setCode(request.code());
        model.setName(request.name());
        model.setModelFamily(request.modelFamily());
        model.setContextWindow(request.contextWindow());
        model.setInputPricePer1m(request.inputPricePer1m());
        model.setOutputPricePer1m(request.outputPricePer1m());
        model.setStatus("ACTIVE");
        aiModelMapper.insert(model);
        return model;
    }

    /**
     * 查询全部模型列表，按ID升序排列。
     *
     * @return 模型列表
     */
    public List<AiModel> list() {
        return aiModelMapper.selectList(Wrappers.<AiModel>lambdaQuery().orderByAsc(AiModel::getId));
    }

    /**
     * 根据供应商ID查询该供应商下的模型列表，按ID升序排列。
     *
     * @param providerId 供应商ID
     * @return 该供应商下的模型列表
     */
    public List<AiModel> listByProviderId(Long providerId) {
        return aiModelMapper.selectList(
                Wrappers.<AiModel>lambdaQuery()
                        .eq(AiModel::getProviderId, providerId)
                        .orderByAsc(AiModel::getId)
        );
    }

    /**
     * 根据ID查询模型，若不存在则抛出异常。
     *
     * @param modelId 模型ID
     * @return 模型实体
     * @throws AiModelNotFoundException 当模型不存在时抛出
     */
    public AiModel getByIdOrThrow(Long modelId) {
        AiModel model = aiModelMapper.selectById(modelId);
        if (model == null) {
            throw new AiModelNotFoundException(modelId);
        }
        return model;
    }
}
