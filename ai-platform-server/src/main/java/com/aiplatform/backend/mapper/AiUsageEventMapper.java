package com.aiplatform.backend.mapper;

import com.aiplatform.backend.entity.AiUsageEvent;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * AI 用量明细 Mapper。
 */
@Mapper
public interface AiUsageEventMapper extends BaseMapper<AiUsageEvent> {
}
