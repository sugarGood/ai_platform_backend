package com.aiplatform.backend.common.exception;

import com.aiplatform.backend.common.dto.ApiResult;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 全局异常处理：业务异常 {@link BusinessException} 与校验异常等统一转为 {@link ApiResult}。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResult<Void>> handleBusiness(BusinessException ex) {
        return ResponseEntity.status(ex.getHttpStatus())
                .body(ApiResult.fail(ex.getHttpStatus(), ex.getErrorCode(), ex.getMessage()));
    }

    /**
     * 参数校验失败：{@code errorCode} 固定为 {@link BizErrorCode#VALIDATION_FAILED}，
     * {@code data} 为字段 → 错误文案的 Map，便于表单逐行提示。
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResult<Map<String, String>>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        err -> err.getField(),
                        err -> err.getDefaultMessage() != null ? err.getDefaultMessage() : "invalid",
                        (a, b) -> a + "; " + b,
                        LinkedHashMap::new));
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("Validation failed");
        return ResponseEntity.badRequest()
                .body(ApiResult.fail(400, BizErrorCode.VALIDATION_FAILED, message, fieldErrors));
    }

    /**
     * 未改为 {@link BusinessException} 的运行时异常：若类上有 {@link ResponseStatus} 则沿用其 HTTP 状态，
     * {@code errorCode} 由类名推导（大写下划线），避免完全无码可查。
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResult<Void>> handleRuntime(RuntimeException ex) {
        ResponseStatus annotation = AnnotationUtils.findAnnotation(ex.getClass(), ResponseStatus.class);
        if (annotation != null) {
            HttpStatus status = annotation.value();
            String msg = ex.getMessage() != null ? ex.getMessage() : status.getReasonPhrase();
            String code = camelToUpperSnake(ex.getClass().getSimpleName().replace("Exception", ""));
            return ResponseEntity.status(status)
                    .body(ApiResult.fail(status.value(), code, msg));
        }
        String msg = ex.getMessage() != null ? ex.getMessage() : HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResult.fail(500, BizErrorCode.INTERNAL_ERROR, msg));
    }

    private static String camelToUpperSnake(String name) {
        if (name == null || name.isEmpty()) {
            return BizErrorCode.INTERNAL_ERROR;
        }
        return name.replaceAll("([a-z\\d])([A-Z])", "$1_$2").toUpperCase();
    }
}
