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
 * 项目服务实体，对应数据库 {@code services} 表。
 *
 * <p>记录项目下各微服务/应用的基本信息，包括代码仓库地址、技术栈等。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("services")
public class ServiceEntity {

    @TableId(type = IdType.AUTO)
    private Long id;              // 服务主键 ID（自增）
    private Long projectId;       // 所属项目 ID
    private String name;          // 服务名称
    private String description;   // 服务描述
    private String gitRepoUrl;    // Git 仓库地址
    private String mainBranch;    // 主分支名称，默认 "main"
    private String framework;     // 技术框架（如 Spring Boot、Express 等）
    private String language;      // 编程语言（如 Java、TypeScript 等）
    private String status;        // 服务状态：ACTIVE / INACTIVE
    private LocalDateTime createdAt;  // 创建时间
    private LocalDateTime updatedAt;  // 最后更新时间

}
