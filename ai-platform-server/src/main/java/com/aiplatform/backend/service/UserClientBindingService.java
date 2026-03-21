package com.aiplatform.backend.service;

import com.aiplatform.backend.dto.CreateUserClientBindingRequest;
import com.aiplatform.backend.entity.UserClientBinding;
import com.aiplatform.backend.mapper.UserClientBindingMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户客户端绑定业务服务。
 */
@Service
public class UserClientBindingService {

    private final UserClientBindingMapper userClientBindingMapper;

    /** 构造函数。 */
    public UserClientBindingService(UserClientBindingMapper userClientBindingMapper) {
        this.userClientBindingMapper = userClientBindingMapper;
    }

    /** 创建用户客户端绑定。 */
    public UserClientBinding create(CreateUserClientBindingRequest request) {
        UserClientBinding binding = new UserClientBinding();
        binding.setUserId(request.userId());
        binding.setClientAppId(request.clientAppId());
        binding.setBindingStatus("ACTIVE");
        userClientBindingMapper.insert(binding);
        return binding;
    }

    /** 按用户 ID 查询绑定列表。 */
    public List<UserClientBinding> listByUserId(Long userId) {
        return userClientBindingMapper.selectList(
                Wrappers.<UserClientBinding>lambdaQuery()
                        .eq(UserClientBinding::getUserId, userId)
                        .orderByAsc(UserClientBinding::getId)
        );
    }

    /** 删除绑定。 */
    public void delete(Long id) {
        userClientBindingMapper.deleteById(id);
    }
}
