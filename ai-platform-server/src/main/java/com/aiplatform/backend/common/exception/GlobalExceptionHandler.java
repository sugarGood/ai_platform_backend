package com.aiplatform.backend.common.exception;

import com.aiplatform.backend.common.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器，拦截控制器抛出的异常并转换为统一的 {@link ErrorResponse} 格式返回。
 *
 * <p>覆盖以下场景：</p>
 * <ul>
 *     <li>请求参数校验失败（{@link MethodArgumentNotValidException}）</li>
 *     <li>业务运行时异常（带 {@link ResponseStatus} 注解或通用 {@link RuntimeException}）</li>
 * </ul>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理请求参数校验失败异常，将所有字段错误拼接为一条消息后返回 400。
     *
     * @param ex 校验失败异常
     * @return 包含错误详情的 400 响应
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("Validation failed");
        return ResponseEntity.badRequest()
                .body(ErrorResponse.of(400, "Bad Request", message));
    }

    /**
     * 处理运行时异常。若异常类标注了 {@link ResponseStatus}，则使用注解中的状态码；
     * 否则统一返回 500 Internal Server Error。
     *
     * @param ex 运行时异常
     * @return 包含错误详情的响应
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntime(RuntimeException ex) {
        ResponseStatus annotation = ex.getClass().getAnnotation(ResponseStatus.class);
        if (annotation != null) {
            HttpStatus status = annotation.value();
            return ResponseEntity.status(status)
                    .body(ErrorResponse.of(status.value(), status.getReasonPhrase(), ex.getMessage()));
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of(500, "Internal Server Error", ex.getMessage()));
    }
}
