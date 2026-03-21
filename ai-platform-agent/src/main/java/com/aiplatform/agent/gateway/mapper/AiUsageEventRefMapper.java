package com.aiplatform.agent.gateway.mapper;

import com.aiplatform.agent.gateway.entity.AiUsageEventRef;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * AI 用量事件数据访问接口。
 *
 * <p>继承 MyBatis-Plus {@link BaseMapper}，提供对 {@code ai_usage_events} 表的
 * 基础 CRUD 操作。网关模块通过该 Mapper 在每次调用完成后插入用量事件记录。</p>
 */
@Mapper
public interface AiUsageEventRefMapper extends BaseMapper<AiUsageEventRef> {
}
