package com.aiplatform.backend.common.web;

import com.aiplatform.backend.common.dto.ApiResult;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * 将控制器与全局异常处理器的返回值统一包装为 {@link ApiResult}。
 *
 * <p>例外：</p>
 * <ul>
 *   <li>已是 {@link ApiResult} 的不再二次包装</li>
 *   <li>标注 {@link ResponseStatus}{@code NO_CONTENT} 或 204：不写入 body（符合 HTTP 语义）</li>
 *   <li>返回类型为 {@link String}：避免与 {@code StringHttpMessageConverter} 冲突，保持原样</li>
 * </ul>
 */
@RestControllerAdvice(basePackages = {
        "com.aiplatform.backend.controller",
        "com.aiplatform.backend.common.exception"
})
public class UnifiedResponseBodyAdvice implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(
            @NonNull MethodParameter returnType,
            @NonNull Class<? extends HttpMessageConverter<?>> converterType) {
        return !CharSequence.class.isAssignableFrom(returnType.getParameterType());
    }

    @Override
    public Object beforeBodyWrite(
            @Nullable Object body,
            @NonNull MethodParameter returnType,
            @NonNull MediaType selectedContentType,
            @NonNull Class<? extends HttpMessageConverter<?>> selectedConverterType,
            @NonNull ServerHttpRequest request,
            @NonNull ServerHttpResponse response) {

        ResponseStatus responseStatus = returnType.getMethodAnnotation(ResponseStatus.class);
        if (responseStatus != null && responseStatus.value() == HttpStatus.NO_CONTENT) {
            return null;
        }

        if (body instanceof ApiResult<?>) {
            return body;
        }

        return ApiResult.ok(body);
    }
}
