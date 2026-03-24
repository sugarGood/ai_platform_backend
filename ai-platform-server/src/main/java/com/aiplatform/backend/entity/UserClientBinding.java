package com.aiplatform.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户客户端绑定实体，对应 {@code user_client_bindings} 表。
 *
 * <p>记录用户与研发客户端的绑定关系和活跃状态。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("user_client_bindings")
public class UserClientBinding {

    /** 主键 ID */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 用户 ID */
    private Long userId;
    /** 客户端 ID */
    private Long clientAppId;
    /** 绑定状态：ACTIVE / INACTIVE / REVOKED */
    private String bindingStatus;
    /** 最后活跃时间 */
    private LocalDateTime lastActiveAt;
    /** 创建时间 */
    private LocalDateTime createdAt;
    /** 最后更新时间 */
    private LocalDateTime updatedAt;

}
