package com.aiplatform.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 创建知识库文档请求 DTO。
 *
 * @param kbId       所属知识库ID（必填）
 * @param title      文档标题（必填）
 * @param fileType   文件类型：pdf / docx / md 等（必填）
 * @param filePath   文件存储路径
 * @param fileSize   文件大小（字节）
 * @param injectMode 注入方式，默认 ON_DEMAND
 */
public record CreateKbDocumentRequest(
        @NotNull Long kbId,
        @NotBlank String title,
        @NotBlank String fileType,
        String filePath,
        Long fileSize,
        String injectMode
) {
}
