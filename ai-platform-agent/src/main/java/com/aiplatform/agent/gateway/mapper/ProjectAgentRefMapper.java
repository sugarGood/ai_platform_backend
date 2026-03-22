package com.aiplatform.agent.gateway.mapper;

import com.aiplatform.agent.gateway.entity.ProjectAgentRef;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 项目专属智能体 Mapper（网关只读）。
 */
@Mapper
public interface ProjectAgentRefMapper extends BaseMapper<ProjectAgentRef> {
}
