package com.aiplatform.backend.mapper;

import com.aiplatform.backend.entity.ToolDefinition;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 工具定义数据访问接口。
 *
 * <p>继承 MyBatis-Plus 的 {@link BaseMapper}，提供工具定义表的基础 CRUD 操作。</p>
 */
@Mapper
public interface ToolDefinitionMapper extends BaseMapper<ToolDefinition> {
}
