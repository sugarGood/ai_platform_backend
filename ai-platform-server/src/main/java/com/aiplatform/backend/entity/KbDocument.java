package com.aiplatform.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * 知识库文档实体，对应 kb_documents 表。
 *
 * <p>表示知识库中的单个文档，记录文档的元信息、处理状态和检索统计。
 * 文档上传后经过分块处理，用于 AI 检索增强。</p>
 */
@TableName("kb_documents")
public class KbDocument {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属知识库ID */
    private Long kbId;

    /** 文档标题 */
    private String title;

    /** 文件类型：pdf / docx / md 等 */
    private String fileType;

    /** 文件存储路径 */
    private String filePath;

    /** 文件大小（字节） */
    private Long fileSize;

    /** 分块数量 */
    private Integer chunkCount;

    /** 被检索命中次数 */
    private Integer hitCount;

    /** 注入方式：AUTO_INJECT / ON_DEMAND / DISABLED */
    private String injectMode;

    /** 被引用的项目数 */
    private Integer refProjects;

    /** 处理状态：PENDING（待处理）/ PROCESSING（处理中）/ READY（就绪）/ ERROR（出错） */
    private String status;

    /** 处理错误信息 */
    private String errorMessage;

    /** 上传者用户ID */
    private Long uploadedBy;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 最后更新时间 */
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getKbId() { return kbId; }
    public void setKbId(Long kbId) { this.kbId = kbId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
    public Integer getChunkCount() { return chunkCount; }
    public void setChunkCount(Integer chunkCount) { this.chunkCount = chunkCount; }
    public Integer getHitCount() { return hitCount; }
    public void setHitCount(Integer hitCount) { this.hitCount = hitCount; }
    public String getInjectMode() { return injectMode; }
    public void setInjectMode(String injectMode) { this.injectMode = injectMode; }
    public Integer getRefProjects() { return refProjects; }
    public void setRefProjects(Integer refProjects) { this.refProjects = refProjects; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public Long getUploadedBy() { return uploadedBy; }
    public void setUploadedBy(Long uploadedBy) { this.uploadedBy = uploadedBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
