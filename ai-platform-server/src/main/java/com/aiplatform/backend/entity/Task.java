package com.aiplatform.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 敏捷任务实体，对应 tasks 表。 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("tasks")
public class Task {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long projectId;
    private Long epicId;
    private Long sprintId;
    private String title;
    private String description;
    private String status; // TODO / IN_PROGRESS / DONE
    private String priority; // LOW / MEDIUM / HIGH / CRITICAL
    private Long assigneeUserId;
    private LocalDate dueDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
