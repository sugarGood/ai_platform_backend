package com.aiplatform.backend.controller;

import com.aiplatform.backend.dto.ClientAppResponse;
import com.aiplatform.backend.dto.CreateClientAppRequest;
import com.aiplatform.backend.service.ClientAppService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 研发客户端管理控制器。
 *
 * <p>路径前缀：{@code /api/client-apps}</p>
 */
@RestController
@RequestMapping("/api/client-apps")
public class ClientAppController {

    private final ClientAppService clientAppService;

    /** 构造函数。 */
    public ClientAppController(ClientAppService clientAppService) {
        this.clientAppService = clientAppService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ClientAppResponse create(@Valid @RequestBody CreateClientAppRequest request) {
        return ClientAppResponse.from(clientAppService.create(request));
    }

    @GetMapping
    public List<ClientAppResponse> list() {
        return clientAppService.list().stream()
                .map(ClientAppResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public ClientAppResponse getById(@PathVariable Long id) {
        return ClientAppResponse.from(clientAppService.getByIdOrThrow(id));
    }
}
