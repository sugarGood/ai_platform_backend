package com.aiplatform.backend.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 研发客户端不存在异常。
 *
 * <p>当根据 ID 查询客户端但未找到记录时抛出，HTTP 状态码返回 404。</p>
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ClientAppNotFoundException extends RuntimeException {

    /**
     * @param clientAppId 未找到的客户端 ID
     */
    public ClientAppNotFoundException(Long clientAppId) {
        super("Client app not found: " + clientAppId);
    }
}
