package com.aiplatform.backend.mapper;

import com.aiplatform.backend.entity.Role;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 角色数据访问接口。
 *
 * <p>继承 MyBatis-Plus 的 {@link BaseMapper}，提供角色表（roles）的基础 CRUD 操作。</p>
 */
@Mapper
public interface RoleMapper extends BaseMapper<Role> {

    /**
     * 根据角色编码查询平台级角色 ID。
     *
     * @param code      角色编码，如 SUPER_ADMIN / PLATFORM_ADMIN / MEMBER
     * @return 角色 ID，不存在时返回 {@code null}
     */
    @Select("SELECT id FROM roles WHERE code = #{code} AND role_scope = 'PLATFORM' AND status = 'ACTIVE' LIMIT 1")
    Long findIdByCode(@Param("code") String code);

    /**
     * 根据角色 ID 查询角色编码。
     *
     * @param roleId 角色 ID
     * @return 角色编码，不存在时返回 {@code null}
     */
    @Select("SELECT code FROM roles WHERE id = #{roleId} AND status = 'ACTIVE' LIMIT 1")
    String findCodeById(@Param("roleId") Long roleId);
}
