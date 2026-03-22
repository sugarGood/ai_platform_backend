package com.aiplatform.agent.gateway.mapper;

import com.aiplatform.agent.gateway.entity.ProjectKnowledgeConfigRef;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 项目知识库配置数据访问接口。
 *
 * <p>网关通过该 Mapper 查询项目关联了哪些知识库及检索权重。</p>
 */
@Mapper
public interface ProjectKnowledgeConfigRefMapper extends BaseMapper<ProjectKnowledgeConfigRef> {
}
