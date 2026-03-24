package com.aiplatform.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 事故实体，对应 incidents 表。 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("incidents")
public class Incident {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long projectId;
    private Long serviceId;
    private String title;
    private String severity; // CRITICAL / HIGH / MEDIUM / LOW
    private String status;   // OPEN / INVESTIGATING / RESOLVED
    private String errorStack;
    private String errorRequest;
    private String aiDiagnosis;
    private String aiDiagnosisStatus; // PENDING / DONE
    private Long assigneeUserId;
    private String githubIssueUrl;
    private LocalDateTime resolvedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
