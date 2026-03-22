package com.aiplatform.agent.gateway.mapper;

import com.aiplatform.agent.gateway.entity.ProjectRef;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 项目引用数据访问接口。
 *
 * <p>网关通过该 Mapper 读取项目配额信息和原子递增项目 Token 用量。</p>
 */
@Mapper
public interface ProjectRefMapper extends BaseMapper<ProjectRef> {

    /**
     * 原子递增项目当月已用 Token 数。
     *
     * @param id     项目 ID
     * @param tokens 本次消耗的 Token 数
     * @return 影响行数
     */
    @Update("UPDATE projects SET used_tokens_this_month = used_tokens_this_month + #{tokens} WHERE id = #{id}")
    int incrementUsedTokens(@Param("id") Long id, @Param("tokens") long tokens);
}
