package com.aiplatform.backend.dto;

import com.aiplatform.backend.entity.UserClientBinding;

import java.time.LocalDateTime;

/**
 * 用户客户端绑定响应 DTO。
 *
 * @param id            绑定 ID
 * @param userId        用户 ID
 * @param clientAppId   客户端 ID
 * @param bindingStatus 绑定状态
 * @param lastActiveAt  最后活跃时间
 * @param createdAt     创建时间
 * @param updatedAt     更新时间
 */
public record UserClientBindingResponse(
        Long id,
        Long userId,
        Long clientAppId,
        String bindingStatus,
        LocalDateTime lastActiveAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static UserClientBindingResponse from(UserClientBinding binding) {
        return new UserClientBindingResponse(
                binding.getId(),
                binding.getUserId(),
                binding.getClientAppId(),
                binding.getBindingStatus(),
                binding.getLastActiveAt(),
                binding.getCreatedAt(),
                binding.getUpdatedAt()
        );
    }
}
