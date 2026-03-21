package com.aiplatform.agent.gateway.mapper;

import com.aiplatform.agent.gateway.entity.AiModelRef;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * AI 模型引用数据访问接口。
 *
 * <p>继承 MyBatis-Plus {@link BaseMapper}，提供对 {@code ai_models} 表的
 * 基础 CRUD 操作。网关模块通过该 Mapper 按模型编码查找对应的模型记录。</p>
 */
@Mapper
public interface AiModelRefMapper extends BaseMapper<AiModelRef> {
}
