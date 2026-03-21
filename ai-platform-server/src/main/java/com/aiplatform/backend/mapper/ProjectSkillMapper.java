package com.aiplatform.backend.mapper;

import com.aiplatform.backend.entity.ProjectSkill;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 项目技能数据访问接口。
 *
 * <p>继承 MyBatis-Plus 的 {@link BaseMapper}，提供项目技能关联表的基础 CRUD 操作。</p>
 */
@Mapper
public interface ProjectSkillMapper extends BaseMapper<ProjectSkill> {
}
