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
 * 部门实体，对应 departments 表。
 * <p>管理企业内部的组织架构部门信息。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("departments")
public class Department {

    /** 主键ID，自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 部门名称 */
    private String name;

    /** 部门编码，用于唯一标识 */
    private String code;

    /** 部门描述 */
    private String description;

    /** 状态：ACTIVE（启用）/ DISABLED（禁用） */
    private String status;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;

}
