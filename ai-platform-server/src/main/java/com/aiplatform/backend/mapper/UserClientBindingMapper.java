package com.aiplatform.backend.mapper;

import com.aiplatform.backend.entity.UserClientBinding;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户客户端绑定数据访问接口。
 *
 * <p>继承 MyBatis-Plus {@link BaseMapper}，提供 {@code user_client_bindings} 表的基础 CRUD 操作。</p>
 */
@Mapper
public interface UserClientBindingMapper extends BaseMapper<UserClientBinding> {
}
