package com.aiplatform.backend.mapper;

import com.aiplatform.backend.entity.PermissionDefinition;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 权限点定义数据访问接口。
 */
@Mapper
public interface PermissionDefinitionMapper extends BaseMapper<PermissionDefinition> {

    /**
     * 查询指定角色拥有的所有权限编码（permission_key）。
     *
     * <p>关联链：roles.code → role_permissions.role_id → permission_definitions.permission_key</p>
     *
     * @param roleCode 角色编码，对应 {@code roles.code}，如 SUPER_ADMIN / PLATFORM_ADMIN / MEMBER
     * @return 权限编码列表（如 {@code knowledge.upload}、{@code skill.publish}）
     */
    @Select("SELECT pd.permission_key " +
            "FROM permission_definitions pd " +
            "INNER JOIN role_permissions rp ON rp.permission_id = pd.id " +
            "INNER JOIN roles r ON r.id = rp.role_id " +
            "WHERE r.code = #{roleCode} AND r.status = 'ACTIVE'")
    List<String> findPermissionCodesByRoleCode(@Param("roleCode") String roleCode);
}
