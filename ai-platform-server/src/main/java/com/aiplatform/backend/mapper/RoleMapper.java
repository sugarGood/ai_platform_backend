package com.aiplatform.backend.mapper;

import com.aiplatform.backend.entity.Role;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 角色数据访问接口。
 *
 * <p>继承 MyBatis-Plus 的 {@link BaseMapper}，提供角色表（roles）的基础 CRUD 操作。</p>
 */
@Mapper
public interface RoleMapper extends BaseMapper<Role> {
}
