package com.aiplatform.backend.dto;

import com.aiplatform.backend.entity.Department;

import java.time.LocalDateTime;

/**
 * 部门信息响应 DTO。
 * <p>封装部门实体的全部字段，用于 REST 接口返回。</p>
 *
 * @param id          部门ID
 * @param name        部门名称
 * @param code        部门编码
 * @param description 部门描述
 * @param status      状态
 * @param createdAt   创建时间
 * @param updatedAt   更新时间
 */
public record DepartmentResponse(
        Long id,
        String name,
        String code,
        String description,
        String status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    /**
     * 将部门实体转换为响应 DTO。
     *
     * @param department 部门实体
     * @return 部门响应 DTO
     */
    public static DepartmentResponse from(Department department) {
        return new DepartmentResponse(
                department.getId(),
                department.getName(),
                department.getCode(),
                department.getDescription(),
                department.getStatus(),
                department.getCreatedAt(),
                department.getUpdatedAt()
        );
    }
}
