package com.aiplatform.agent.gateway.mapper;

import com.aiplatform.agent.gateway.entity.ProviderApiKeyRef;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 上游 API 密钥引用数据访问接口。
 *
 * <p>继承 MyBatis-Plus {@link BaseMapper}，提供对 {@code provider_api_keys} 表的
 * 基础 CRUD 操作。网关模块通过该 Mapper 获取供应商对应的可用 API 密钥。</p>
 */
@Mapper
public interface ProviderApiKeyRefMapper extends BaseMapper<ProviderApiKeyRef> {
}
