package com.aiplatform.backend.mapper;

import com.aiplatform.backend.entity.PermissionDefinition;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 权限点定义数据访问接口。
 *
 * <p>继承 MyBatis-Plus 的 {@link BaseMapper}，提供权限点定义表（permission_definitions）的基础 CRUD 操作。</p>
 */
@Mapper
public interface PermissionDefinitionMapper extends BaseMapper<PermissionDefinition> {
}
