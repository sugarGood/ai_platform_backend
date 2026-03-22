package com.aiplatform.agent.gateway.mapper;

import com.aiplatform.agent.gateway.entity.PlatformCredentialRef;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 平台凭证引用数据访问接口。
 *
 * <p>继承 MyBatis-Plus {@link BaseMapper}，提供对 {@code platform_credentials} 表的
 * 基础 CRUD 操作。网关模块主要通过该 Mapper 按哈希值查询有效凭证，
 * 以及原子递增个人 Token 用量。</p>
 */
@Mapper
public interface PlatformCredentialRefMapper extends BaseMapper<PlatformCredentialRef> {

    /**
     * 原子递增个人当月已用 Token 数。
     *
     * @param id     凭证 ID
     * @param tokens 本次消耗的 Token 数
     * @return 影响行数
     */
    @Update("UPDATE platform_credentials SET used_tokens_this_month = used_tokens_this_month + #{tokens} WHERE id = #{id}")
    int incrementUsedTokens(@Param("id") Long id, @Param("tokens") long tokens);
}
