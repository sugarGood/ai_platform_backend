package com.aiplatform.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 代码模板实体，对应 project_templates 表。 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("project_templates")
public class ProjectTemplate {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String description;
    private String templateType;
    private String scope;
    private Long projectId;
    private String language;
    private String framework;
    private String templateContent;
    private String gitUrl;
    private Integer downloadCount;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
