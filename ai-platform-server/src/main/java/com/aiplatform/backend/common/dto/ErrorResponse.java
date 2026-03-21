package com.aiplatform.backend.common.dto;

import java.time.LocalDateTime;

/**
 * 统一错误响应体，在全局异常处理器中构建并返回给客户端。
 *
 * @param status    HTTP 状态码（如 400、404、500）
 * @param error     错误类别简述（如 "Bad Request"、"Not Found"）
 * @param message   具体错误信息，供前端展示或调试使用
 * @param timestamp 错误发生的时间戳
 */
public record ErrorResponse(int status, String error, String message, LocalDateTime timestamp) {

    /**
     * 快捷工厂方法，自动填充当前时间戳。
     *
     * @param status  HTTP 状态码
     * @param error   错误类别简述
     * @param message 具体错误信息
     * @return 错误响应实例
     */
    public static ErrorResponse of(int status, String error, String message) {
        return new ErrorResponse(status, error, message, LocalDateTime.now());
    }
}
