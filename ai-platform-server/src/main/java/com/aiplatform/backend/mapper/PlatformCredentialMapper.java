package com.aiplatform.backend.mapper;

import com.aiplatform.backend.entity.PlatformCredential;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 平台凭证数据访问接口。
 *
 * <p>继承 MyBatis-Plus {@link BaseMapper}，提供 {@code platform_credentials} 表的基础 CRUD 操作。</p>
 */
@Mapper
public interface PlatformCredentialMapper extends BaseMapper<PlatformCredential> {
}
