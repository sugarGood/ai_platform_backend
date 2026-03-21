package com.aiplatform.agent.gateway.mapper;

import com.aiplatform.agent.gateway.entity.PlatformCredentialRef;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 平台凭证引用数据访问接口。
 *
 * <p>继承 MyBatis-Plus {@link BaseMapper}，提供对 {@code platform_credentials} 表的
 * 基础 CRUD 操作。网关模块主要通过该 Mapper 按哈希值查询有效凭证。</p>
 */
@Mapper
public interface PlatformCredentialRefMapper extends BaseMapper<PlatformCredentialRef> {
}
