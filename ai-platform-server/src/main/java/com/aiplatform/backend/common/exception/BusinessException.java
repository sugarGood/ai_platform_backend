package com.aiplatform.backend.common.exception;

/**
 * 平台业务异常基类：携带 HTTP 状态、稳定错误码与可读文案，供全局处理为 {@link com.aiplatform.backend.common.dto.ApiResult}。
 *
 * <p>前端建议优先根据 {@code errorCode} 做分支（弹窗、跳转、i18n key），{@code message} 作兜底展示。</p>
 */
public class BusinessException extends RuntimeException {

    private final int httpStatus;
    private final String errorCode;

    public BusinessException(int httpStatus, String errorCode, String message) {
        super(message);
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
    }

    public BusinessException(int httpStatus, String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
