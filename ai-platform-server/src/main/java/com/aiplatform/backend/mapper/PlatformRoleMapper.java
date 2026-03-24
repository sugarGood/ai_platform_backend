package com.aiplatform.backend.mapper;

import com.aiplatform.backend.entity.PlatformRole;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 平台角色数据访问接口。
 */
@Mapper
public interface PlatformRoleMapper extends BaseMapper<PlatformRole> {
}
