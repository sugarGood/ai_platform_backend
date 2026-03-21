package com.aiplatform.backend.mapper;

import com.aiplatform.backend.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户数据访问接口。
 * <p>继承 MyBatis Plus 的 {@link BaseMapper}，提供用户表的基础 CRUD 操作。</p>
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
