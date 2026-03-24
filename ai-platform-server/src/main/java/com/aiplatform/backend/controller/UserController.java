package com.aiplatform.backend.controller;

import com.aiplatform.backend.common.annotation.RequireRole;
import com.aiplatform.backend.common.security.AuthContext;
import com.aiplatform.backend.dto.AdminResetPasswordRequest;
import com.aiplatform.backend.dto.CreateUserRequest;
import com.aiplatform.backend.dto.CreateUserResponse;
import com.aiplatform.backend.dto.UpdateUserPlatformRoleRequest;
import com.aiplatform.backend.dto.UpdateUserRequest;
import com.aiplatform.backend.dto.UserResponse;
import com.aiplatform.backend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

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
     * 查询用户列表，支持按关键词、部门、角色、状态筛选。
     *
     * @param keyword      搜索关键词（姓名/邮箱/用户名，可选）
     * @param departmentId 部门ID过滤（可选）
     * @param platformRole 平台角色过滤（可选）
     * @param status       状态过滤（可选）
     * @return 用户响应列表
     */
    @GetMapping
    public List<UserResponse> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) String platformRole,
            @RequestParam(required = false) String status) {
        return userService.search(keyword, departmentId, platformRole, status)
                .stream().map(UserResponse::from).toList();
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

    /**
     * 切换用户账号状态（启用/停用）。
     *
     * @param id   用户ID
     * @param body 请求体，包含 {@code status} 字段（ACTIVE / DISABLED）
     * @return 更新后的用户响应
     */
    @PatchMapping("/{id}/status")
    public UserResponse updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String newStatus = body.get("status");
        return UserResponse.from(userService.updateStatus(id, newStatus));
    }

    /**
     * 管理员重置指定用户的登录密码（无需原密码）。
     *
     * <p>需 {@code SUPER_ADMIN} 或 {@code PLATFORM_ADMIN}；平台管理员不可重置其它管理员或超级管理员。</p>
     *
     * @param id      用户ID
     * @param request 新密码
     * @return 更新后的用户（不含密码字段）
     */
    @PostMapping("/{id}/reset-password")
    @RequireRole({"SUPER_ADMIN", "PLATFORM_ADMIN"})
    public UserResponse resetPassword(@PathVariable Long id,
                                      @Valid @RequestBody AdminResetPasswordRequest request) {
        return UserResponse.from(userService.adminResetPassword(AuthContext.getRole(), id, request.newPassword()));
    }

    /**
     * 修改用户平台角色（同步 {@code users.role_id}）。
     *
     * <p>仅 {@code SUPER_ADMIN} 可调用。</p>
     *
     * @param id      用户ID
     * @param request 目标 {@code platformRole} 编码
     * @return 更新后的用户
     */
    @PatchMapping("/{id}/platform-role")
    @RequireRole("SUPER_ADMIN")
    public UserResponse updatePlatformRole(@PathVariable Long id,
                                           @Valid @RequestBody UpdateUserPlatformRoleRequest request) {
        return UserResponse.from(
                userService.updatePlatformRole(AuthContext.getRole(), id, request.platformRole()));
    }

    /**
     * 邀请用户（创建 INACTIVE 状态用户，发送邀请邮件）。
     *
     * @param request 邀请请求，包含 email、departmentId、platformRole 等
     * @return 创建的用户响应（含凭证明文密钥，仅此一次）
     */
    @PostMapping("/invite")
    @ResponseStatus(HttpStatus.CREATED)
    public CreateUserResponse invite(@Valid @RequestBody CreateUserRequest request) {
        return userService.invite(request);
    }

    /**
     * 重发邀请邮件。
     *
     * @param id 用户ID
     * @return 操作结果消息
     */
    @PostMapping("/{id}/reinvite")
    public Map<String, String> reinvite(@PathVariable Long id) {
        userService.reinvite(id);
        return Map.of("message", "邀请邮件已重新发送");
    }
}
