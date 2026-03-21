package com.aiplatform.backend.mapper;

import com.aiplatform.backend.entity.Project;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 项目数据访问接口，继承 MyBatis Plus 的 {@link BaseMapper} 以获取通用 CRUD 能力。
 *
 * <p>对应实体：{@link Project}，映射数据库 {@code projects} 表。</p>
 */
@Mapper
public interface ProjectMapper extends BaseMapper<Project> {
}
