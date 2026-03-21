package com.aiplatform.backend.controller;

import com.aiplatform.backend.dto.CreateUserRequest;
import com.aiplatform.backend.dto.CreateUserResponse;
import com.aiplatform.backend.dto.UpdateUserRequest;
import com.aiplatform.backend.dto.UserResponse;
import com.aiplatform.backend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 用户管理控制器。
 * <p>提供 {@code /api/users} 下的 REST 端点，支持用户新增、查询和更新操作。</p>
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 新增用户，同时自动分配平台凭证（一人一证）。
     *
     * <p>响应中的 {@code credentialPlainKey} 为凭证明文密钥，<b>仅此一次</b>，请妥善告知用户保存。</p>
     *
     * @param request 新增用户请求参数
     * @return 包含用户信息和凭证明文密钥的响应
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateUserResponse create(@Valid @RequestBody CreateUserRequest request) {
        return userService.create(request);
    }

    /**
     * 查询全部用户列表。
     *
     * @return 用户响应列表
     */
    @GetMapping
    public List<UserResponse> list() {
        return userService.list().stream().map(UserResponse::from).toList();
    }

    /**
     * 根据ID查询用户详情。
     *
     * @param id 用户ID
     * @return 用户响应
     */
    @GetMapping("/{id}")
    public UserResponse getById(@PathVariable Long id) {
        return UserResponse.from(userService.getByIdOrThrow(id));
    }

    /**
     * 更新指定用户的信息。
     *
     * @param id      用户ID
     * @param request 更新请求参数
     * @return 更新后的用户响应
     */
    @PutMapping("/{id}")
    public UserResponse update(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest request) {
        return UserResponse.from(userService.update(id, request));
    }
}
