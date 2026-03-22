package com.aiplatform.agent.gateway.mapper;

import com.aiplatform.agent.gateway.entity.ProjectSkillRef;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 项目技能关联数据访问接口。
 *
 * <p>网关通过该 Mapper 查询项目启用了哪些技能。</p>
 */
@Mapper
public interface ProjectSkillRefMapper extends BaseMapper<ProjectSkillRef> {
}
