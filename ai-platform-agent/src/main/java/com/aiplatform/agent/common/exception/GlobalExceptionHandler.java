package com.aiplatform.agent.common.exception;

import com.aiplatform.agent.gateway.service.CredentialAuthService;
import com.aiplatform.agent.gateway.service.GatewayRoutingService;
import com.aiplatform.agent.gateway.service.ProjectAccessValidationService;
import com.aiplatform.agent.gateway.service.QuotaCheckService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.io.IOException;
import java.util.Map;

/**
 * 全局异常处理器。
 *
 * <p>统一捕获并处理应用内所有控制器层抛出的异常，返回结构化的 JSON 错误响应。
 * 各控制器无需再重复声明 {@code @ExceptionHandler}，保持控制器职责纯粹。</p>
 *
 * <p>异常处理优先级（由具体到通用）：</p>
 * <ol>
 *   <li>业务异常（认证、配额、路由、智能体等）</li>
 *   <li>请求格式异常（缺少参数、类型不匹配、请求体不可读）</li>
 *   <li>IO 异常</li>
 *   <li>兜底：未预期的运行时异常 / 通用异常</li>
 * </ol>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ---------------------------------------------------------------
    // 1. 业务异常
    // ---------------------------------------------------------------

    /**
     * 凭证无效（401 Unauthorized）。
     */
    @ExceptionHandler(CredentialAuthService.InvalidCredentialException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidCredential(
            CredentialAuthService.InvalidCredentialException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(error(ex.getMessage()));
    }

    /**
     * 配额超限（429 Too Many Requests）。
     */
    @ExceptionHandler(QuotaCheckService.QuotaExceededException.class)
    public ResponseEntity<Map<String, Object>> handleQuotaExceeded(
            QuotaCheckService.QuotaExceededException ex) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(error(ex.getMessage(), ex.getCode().name()));
    }

    /**
     * 模型未找到（404 Not Found）。
     */
    @ExceptionHandler(GatewayRoutingService.ModelNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleModelNotFound(
            GatewayRoutingService.ModelNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(error(ex.getMessage()));
    }

    /**
     * 上游供应商不可用（503 Service Unavailable）。
     */
    @ExceptionHandler(GatewayRoutingService.ProviderNotAvailableException.class)
    public ResponseEntity<Map<String, Object>> handleProviderNotAvailable(
            GatewayRoutingService.ProviderNotAvailableException ex) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(error(ex.getMessage()));
    }

    /**
     * 网关供应商未配置（502 Bad Gateway）。
     */
    @ExceptionHandler(GatewayProviderNotConfiguredException.class)
    public ResponseEntity<Map<String, Object>> handleProviderNotConfigured(
            GatewayProviderNotConfiguredException ex) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(error(ex.getMessage()));
    }

    /**
     * 项目访问权限不足（403 Forbidden）。
     */
    @ExceptionHandler(ProjectAccessValidationService.ProjectAccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleProjectAccessDenied(
            ProjectAccessValidationService.ProjectAccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(error(ex.getMessage(), "PROJECT_ACCESS_DENIED"));
    }

    // ---------------------------------------------------------------
    // 2. 请求格式异常
    // ---------------------------------------------------------------

    /**
     * 缺少必填请求头（400 Bad Request）。
     */
    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<Map<String, Object>> handleMissingHeader(
            MissingRequestHeaderException ex) {
        return ResponseEntity.badRequest()
                .body(error("缺少必填请求头: " + ex.getHeaderName()));
    }

    /**
     * 缺少必填请求参数（400 Bad Request）。
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, Object>> handleMissingParam(
            MissingServletRequestParameterException ex) {
        return ResponseEntity.badRequest()
                .body(error("缺少必填参数: " + ex.getParameterName()));
    }

    /**
     * 请求参数类型不匹配（400 Bad Request）。
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex) {
        return ResponseEntity.badRequest()
                .body(error("参数类型错误: " + ex.getName()));
    }

    /**
     * 请求体不可读或 JSON 解析失败（400 Bad Request）。
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleNotReadable(
            HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest()
                .body(error("请求体格式错误，请检查 JSON 格式"));
    }

    // ---------------------------------------------------------------
    // 3. IO 异常
    // ---------------------------------------------------------------

    /**
     * 文件读取或 IO 操作失败（400 Bad Request）。
     */
    @ExceptionHandler(IOException.class)
    public ResponseEntity<Map<String, Object>> handleIOException(IOException ex) {
        log.warn("IO 操作失败: {}", ex.getMessage(), ex);
        return ResponseEntity.badRequest()
                .body(error("文件读取失败: " + ex.getMessage()));
    }

    // ---------------------------------------------------------------
    // 4. 兜底异常
    // ---------------------------------------------------------------

    /**
     * 未预期的运行时异常兜底（500 Internal Server Error）。
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex) {
        log.error("未预期的运行时异常: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(error("服务器内部错误，请联系管理员"));
    }

    /**
     * 最终兜底：捕获所有未处理异常（500 Internal Server Error）。
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleException(Exception ex) {
        log.error("未处理异常: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(error("服务器内部错误"));
    }

    // ---------------------------------------------------------------
    // 工具方法
    // ---------------------------------------------------------------

    private static Map<String, Object> error(String message) {
        return Map.of("error", message);
    }

    private static Map<String, Object> error(String message, String reasonCode) {
        return Map.of("error", message, "reasonCode", reasonCode);
    }
}
