package com.aiplatform.backend.mapper;

import com.aiplatform.backend.entity.AiModel;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * AI 模型数据访问接口。
 * <p>继承 MyBatis Plus 的 {@link BaseMapper}，提供 AI 模型表的基础 CRUD 操作。</p>
 */
@Mapper
public interface AiModelMapper extends BaseMapper<AiModel> {
}
