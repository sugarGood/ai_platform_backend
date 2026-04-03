package com.aiplatform.backend.service;

import com.aiplatform.backend.dto.CreateUserClientBindingRequest;
import com.aiplatform.backend.entity.UserClientBinding;
import com.aiplatform.backend.mapper.UserClientBindingMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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

    /**
     * 更新客户端绑定最近活跃时间。
     *
     * <p>若传入客户端 ID，则仅更新该客户端绑定；否则更新该用户全部 ACTIVE 绑定。</p>
     */
    @Transactional(rollbackFor = Throwable.class)
    public void touchLastActiveAt(Long userId, Long clientAppId) {
        var update = Wrappers.<UserClientBinding>lambdaUpdate()
                .set(UserClientBinding::getLastActiveAt, LocalDateTime.now())
                .eq(UserClientBinding::getUserId, userId)
                .eq(UserClientBinding::getBindingStatus, "ACTIVE");
        if (clientAppId != null) {
            update.eq(UserClientBinding::getClientAppId, clientAppId);
        }
        userClientBindingMapper.update(null, update);
    }

    /** 删除绑定。 */
    public void delete(Long id) {
        userClientBindingMapper.deleteById(id);
    }
}
