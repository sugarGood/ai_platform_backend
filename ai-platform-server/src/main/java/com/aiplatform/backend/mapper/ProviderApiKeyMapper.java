package com.aiplatform.backend.mapper;

import com.aiplatform.backend.entity.ProviderApiKey;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 上游 API 密钥数据访问接口。
 * <p>继承 MyBatis Plus 的 {@link BaseMapper}，提供供应商 API 密钥表的基础 CRUD 操作。</p>
 */
@Mapper
public interface ProviderApiKeyMapper extends BaseMapper<ProviderApiKey> {
}
