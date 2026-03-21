package com.aiplatform.backend.mapper;

import com.aiplatform.backend.entity.ServiceEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 项目服务数据访问接口，继承 MyBatis Plus 的 {@link BaseMapper} 以获取通用 CRUD 能力。
 *
 * <p>对应实体：{@link ServiceEntity}，映射数据库 {@code services} 表。</p>
 */
@Mapper
public interface ServiceEntityMapper extends BaseMapper<ServiceEntity> {
}
