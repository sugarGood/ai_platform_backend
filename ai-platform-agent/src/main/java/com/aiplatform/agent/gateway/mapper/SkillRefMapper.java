package com.aiplatform.agent.gateway.mapper;

import com.aiplatform.agent.gateway.entity.SkillRef;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 技能引用数据访问接口。
 *
 * <p>网关通过该 Mapper 读取技能的 System Prompt 等配置，用于上下文增强。</p>
 */
@Mapper
public interface SkillRefMapper extends BaseMapper<SkillRef> {
}
