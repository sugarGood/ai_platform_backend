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
 * 知识库文档实体，对应 kb_documents 表。
 *
 * <p>表示知识库中的单个文档，记录文档的元信息、处理状态和检索统计。
 * 文档上传后经过分块处理，用于 AI 检索增强。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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

}
