package com.aiplatform.backend.common.dto;

/**
 * 统一 API 响应体，成功与失败均使用同一结构，便于前端拦截器处理。
 *
 * @param success   是否成功
 * @param code      HTTP 语义状态码：成功一般为 200；失败与响应状态码一致（400、404、500 等）
 * @param errorCode 失败时的稳定业务码（如 {@code USER_NOT_FOUND}），成功时为 null
 * @param message   提示文案
 * @param data      成功时为业务数据；失败时可选（如字段校验明细 Map）
 * @param timestamp 服务端毫秒时间戳
 * @param <T>       data 类型
 */
public record ApiResult<T>(
        boolean success,
        int code,
        String errorCode,
        String message,
        T data,
        long timestamp
) {

    public static <T> ApiResult<T> ok(T data) {
        return new ApiResult<>(true, 200, null, "OK", data, System.currentTimeMillis());
    }

    public static ApiResult<Void> okEmpty() {
        return ok(null);
    }

    public static <T> ApiResult<T> fail(int code, String errorCode, String message) {
        return fail(code, errorCode, message, null);
    }

    public static <T> ApiResult<T> fail(int code, String errorCode, String message, T data) {
        return new ApiResult<>(false, code, errorCode, message, data, System.currentTimeMillis());
    }
}
