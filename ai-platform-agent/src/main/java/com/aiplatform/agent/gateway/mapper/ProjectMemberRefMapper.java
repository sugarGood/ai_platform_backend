package com.aiplatform.agent.gateway.mapper;

import com.aiplatform.agent.gateway.entity.ProjectMemberRef;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 项目成员引用数据访问接口。
 */
@Mapper
public interface ProjectMemberRefMapper extends BaseMapper<ProjectMemberRef> {
}
