package com.aiplatform.backend.mapper;

import com.aiplatform.backend.entity.ClientApp;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 研发客户端数据访问接口。
 *
 * <p>继承 MyBatis-Plus {@link BaseMapper}，提供 {@code client_apps} 表的基础 CRUD 操作。</p>
 */
@Mapper
public interface ClientAppMapper extends BaseMapper<ClientApp> {
}
