package com.aiplatform.backend.controller;

import com.aiplatform.backend.common.annotation.RequireRole;
import com.aiplatform.backend.common.security.AuthContext;
import com.aiplatform.backend.dto.AdminResetPasswordRequest;
import com.aiplatform.backend.dto.CreateUserRequest;
import com.aiplatform.backend.dto.CreateUserResponse;
import com.aiplatform.backend.dto.SimpleMessageResponse;
import com.aiplatform.backend.dto.UpdateUserPlatformRoleRequest;
import com.aiplatform.backend.dto.UpdateUserRequest;
import com.aiplatform.backend.dto.UpdateUserStatusRequest;
import com.aiplatform.backend.dto.UserResponse;
import com.aiplatform.backend.dto.UserSearchQuery;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateUserResponse create(@Valid @RequestBody CreateUserRequest request) {
        return userService.create(request);
    }

    @GetMapping
    public List<UserResponse> list(UserSearchQuery query) {
        return userService.search(query).stream()
                .map(UserResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public UserResponse getById(@PathVariable Long id) {
        return UserResponse.from(userService.getByIdOrThrow(id));
    }

    @PutMapping("/{id}")
    public UserResponse update(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest request) {
        return UserResponse.from(userService.update(id, request));
    }

    @PatchMapping("/{id}/status")
    public UserResponse updateStatus(@PathVariable Long id,
                                     @Valid @RequestBody UpdateUserStatusRequest request) {
        return UserResponse.from(userService.updateStatus(id, request.status()));
    }

    @PostMapping("/{id}/reset-password")
    @RequireRole({"SUPER_ADMIN", "PLATFORM_ADMIN"})
    public UserResponse resetPassword(@PathVariable Long id,
                                      @Valid @RequestBody AdminResetPasswordRequest request) {
        return UserResponse.from(userService.adminResetPassword(AuthContext.getRole(), id, request.newPassword()));
    }

    @PatchMapping("/{id}/platform-role")
    @RequireRole("SUPER_ADMIN")
    public UserResponse updatePlatformRole(@PathVariable Long id,
                                           @Valid @RequestBody UpdateUserPlatformRoleRequest request) {
        return UserResponse.from(userService.updatePlatformRole(AuthContext.getRole(), id, request.platformRole()));
    }

    @PostMapping("/invite")
    @ResponseStatus(HttpStatus.CREATED)
    public CreateUserResponse invite(@Valid @RequestBody CreateUserRequest request) {
        return userService.invite(request);
    }

    @PostMapping("/{id}/reinvite")
    public SimpleMessageResponse reinvite(@PathVariable Long id) {
        userService.reinvite(id);
        return new SimpleMessageResponse("Invitation email re-sent");
    }
}
