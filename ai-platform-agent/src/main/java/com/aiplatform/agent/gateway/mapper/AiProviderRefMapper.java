package com.aiplatform.agent.gateway.mapper;

import com.aiplatform.agent.gateway.entity.AiProviderRef;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * AI 供应商引用数据访问接口。
 *
 * <p>继承 MyBatis-Plus {@link BaseMapper}，提供对 {@code ai_providers} 表的
 * 基础 CRUD 操作。网关模块通过该 Mapper 查询可用的上游供应商信息。</p>
 */
@Mapper
public interface AiProviderRefMapper extends BaseMapper<AiProviderRef> {
}
