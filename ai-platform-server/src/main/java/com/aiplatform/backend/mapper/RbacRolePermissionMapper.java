package com.aiplatform.backend.mapper;

import com.aiplatform.backend.entity.RbacRolePermission;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 角色-权限关联数据访问接口。
 */
@Mapper
public interface RbacRolePermissionMapper extends BaseMapper<RbacRolePermission> {
}
