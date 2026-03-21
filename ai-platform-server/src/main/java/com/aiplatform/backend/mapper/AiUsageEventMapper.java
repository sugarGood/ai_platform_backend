package com.aiplatform.backend.mapper;

import com.aiplatform.backend.entity.AiUsageEvent;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * AI 用量明细数据访问接口。
 *
 * <p>继承 MyBatis-Plus 的 {@link BaseMapper}，提供 AI 用量事件表的基础 CRUD 操作。</p>
 */
@Mapper
public interface AiUsageEventMapper extends BaseMapper<AiUsageEvent> {
}
