package com.aiplatform.backend.controller;

import com.aiplatform.backend.dto.CreateUserClientBindingRequest;
import com.aiplatform.backend.dto.UserClientBindingResponse;
import com.aiplatform.backend.service.UserClientBindingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 用户客户端绑定管理控制器。
 *
 * <p>路径前缀：{@code /api/user-client-bindings}</p>
 */
@RestController
@RequestMapping("/api/user-client-bindings")
public class UserClientBindingController {

    private final UserClientBindingService userClientBindingService;

    /** 构造函数。 */
    public UserClientBindingController(UserClientBindingService userClientBindingService) {
        this.userClientBindingService = userClientBindingService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserClientBindingResponse create(@Valid @RequestBody CreateUserClientBindingRequest request) {
        return UserClientBindingResponse.from(userClientBindingService.create(request));
    }

    @GetMapping
    public List<UserClientBindingResponse> listByUserId(@RequestParam Long userId) {
        return userClientBindingService.listByUserId(userId).stream()
                .map(UserClientBindingResponse::from)
                .toList();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        userClientBindingService.delete(id);
    }
}
