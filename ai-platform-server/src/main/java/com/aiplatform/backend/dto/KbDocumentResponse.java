package com.aiplatform.backend.dto;

import com.aiplatform.backend.entity.KbDocument;

import java.time.LocalDateTime;

/**
 * 知识库文档响应 DTO。
 *
 * <p>用于向客户端返回文档的详细信息，包括处理状态和检索命中统计。</p>
 *
 * @param id           文档ID
 * @param kbId         所属知识库ID
 * @param title        文档标题
 * @param fileType     文件类型
 * @param filePath     文件存储路径
 * @param fileSize     文件大小（字节）
 * @param chunkCount   分块数量
 * @param hitCount     被检索命中次数
 * @param injectMode   注入方式
 * @param refProjects  被引用项目数
 * @param status       处理状态
 * @param errorMessage 处理错误信息
 * @param uploadedBy   上传者ID
 * @param createdAt    创建时间
 */
public record KbDocumentResponse(
        Long id,
        Long kbId,
        String title,
        String fileType,
        String filePath,
        Long fileSize,
        Integer chunkCount,
        Integer hitCount,
        String injectMode,
        Integer refProjects,
        String status,
        String errorMessage,
        Long uploadedBy,
        LocalDateTime createdAt
) {
    /**
     * 将知识库文档实体转换为响应 DTO。
     *
     * @param doc 知识库文档实体
     * @return 知识库文档响应 DTO
     */
    public static KbDocumentResponse from(KbDocument doc) {
        return new KbDocumentResponse(
                doc.getId(), doc.getKbId(), doc.getTitle(), doc.getFileType(),
                doc.getFilePath(), doc.getFileSize(), doc.getChunkCount(),
                doc.getHitCount(), doc.getInjectMode(), doc.getRefProjects(),
                doc.getStatus(), doc.getErrorMessage(), doc.getUploadedBy(), doc.getCreatedAt()
        );
    }
}
