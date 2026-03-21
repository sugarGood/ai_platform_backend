package com.aiplatform.backend.mapper;

import com.aiplatform.backend.entity.ProjectKnowledgeConfig;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 项目知识库配置数据访问接口。
 *
 * <p>继承 MyBatis-Plus 的 {@link BaseMapper}，提供项目知识库继承配置表的基础 CRUD 操作。</p>
 */
@Mapper
public interface ProjectKnowledgeConfigMapper extends BaseMapper<ProjectKnowledgeConfig> {
}
