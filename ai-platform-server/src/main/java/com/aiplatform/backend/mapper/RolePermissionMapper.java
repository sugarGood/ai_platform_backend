package com.aiplatform.backend.mapper;

import com.aiplatform.backend.entity.RolePermission;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 角色-权限关联数据访问接口。
 *
 * <p>继承 MyBatis-Plus 的 {@link BaseMapper}，提供角色-权限关联表（role_permissions）的基础 CRUD 操作。</p>
 */
@Mapper
public interface RolePermissionMapper extends BaseMapper<RolePermission> {
}
