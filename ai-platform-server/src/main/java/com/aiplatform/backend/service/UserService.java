package com.aiplatform.backend.service;

import com.aiplatform.backend.common.exception.BizErrorCode;
import com.aiplatform.backend.common.exception.BusinessException;
import com.aiplatform.backend.common.exception.ForbiddenException;
import com.aiplatform.backend.common.exception.UserNotFoundException;
import com.aiplatform.backend.dto.CreatePlatformCredentialRequest;
import com.aiplatform.backend.dto.CreatePlatformCredentialResponse;
import com.aiplatform.backend.dto.CreateUserRequest;
import com.aiplatform.backend.dto.CreateUserResponse;
import com.aiplatform.backend.dto.UpdateUserRequest;
import com.aiplatform.backend.dto.UserSearchQuery;
import com.aiplatform.backend.entity.User;
import com.aiplatform.backend.mapper.RoleMapper;
import com.aiplatform.backend.mapper.UserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private static final String DEFAULT_PLATFORM_ROLE = "MEMBER";
    private static final String STATUS_ACTIVE = "ACTIVE";
    private static final String STATUS_INACTIVE = "INACTIVE";
    private static final String STATUS_DISABLED = "DISABLED";
    private static final String PERSONAL_CREDENTIAL_TYPE = "PERSONAL";
    private static final String DEFAULT_CREDENTIAL_SUFFIX = " Credential";
    private static final String ACCOUNT_DISABLED_REASON = "Account disabled, credentials revoked automatically";

    private final UserMapper userMapper;
    private final PlatformCredentialService platformCredentialService;
    private final AuthService authService;
    private final RoleMapper roleMapper;

    public UserService(UserMapper userMapper,
                       PlatformCredentialService platformCredentialService,
                       AuthService authService,
                       RoleMapper roleMapper) {
        this.userMapper = userMapper;
        this.platformCredentialService = platformCredentialService;
        this.authService = authService;
        this.roleMapper = roleMapper;
    }

    @Transactional
    public CreateUserResponse create(CreateUserRequest request) {
        return createUserWithCredential(request, STATUS_ACTIVE, resolveCredentialOwner(request, false));
    }

    public User update(Long id, UpdateUserRequest request) {
        User user = getByIdOrThrow(id);
        applyTextUpdate(request.fullName(), user::setFullName);
        applyTextUpdate(request.avatarUrl(), user::setAvatarUrl);
        if (request.departmentId() != null) {
            user.setDepartmentId(request.departmentId());
        }
        applyTextUpdate(request.jobTitle(), user::setJobTitle);
        applyTextUpdate(request.phone(), user::setPhone);
        updateEmailIfChanged(id, user, request.email());
        updateUsernameIfChanged(id, user, request.username());
        userMapper.updateById(user);

        platformCredentialService.patchPersonalCredentialForUser(
                new PlatformCredentialService.PlatformCredentialPatchCommand(
                        id,
                        request.monthlyTokenQuota(),
                        request.alertThresholdPct(),
                        request.overQuotaStrategy(),
                        normalizeText(request.credentialName())));

        return user;
    }

    public User getByIdOrThrow(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new UserNotFoundException(id);
        }
        return user;
    }

    public List<User> list() {
        return userMapper.selectList(Wrappers.<User>lambdaQuery().orderByAsc(User::getId));
    }

    public List<User> search(UserSearchQuery query) {
        LambdaQueryWrapper<User> wrapper = Wrappers.<User>lambdaQuery();
        if (query == null) {
            wrapper.orderByAsc(User::getId);
            return userMapper.selectList(wrapper);
        }

        String keyword = normalizeText(query.keyword());
        if (keyword != null) {
            wrapper.and(w -> w.like(User::getFullName, keyword)
                    .or().like(User::getEmail, keyword)
                    .or().like(User::getUsername, keyword));
        }
        if (query.departmentId() != null) {
            wrapper.eq(User::getDepartmentId, query.departmentId());
        }
        String platformRole = normalizeText(query.platformRole());
        if (platformRole != null) {
            wrapper.eq(User::getPlatformRole, platformRole);
        }
        String status = normalizeText(query.status());
        if (status != null) {
            wrapper.eq(User::getStatus, status);
        }
        wrapper.orderByAsc(User::getId);
        return userMapper.selectList(wrapper);
    }

    @Transactional
    public User updateStatus(Long id, String newStatus) {
        User user = getByIdOrThrow(id);
        String normalizedStatus = requireNonBlank(newStatus, "Status must not be blank");
        user.setStatus(normalizedStatus);
        userMapper.updateById(user);
        if (STATUS_DISABLED.equals(normalizedStatus)) {
            platformCredentialService.revokeAllByUserId(id, ACCOUNT_DISABLED_REASON);
        }
        return user;
    }

    public User findByEmail(String email) {
        return userMapper.selectOne(Wrappers.<User>lambdaQuery().eq(User::getEmail, email));
    }

    @Transactional
    public CreateUserResponse invite(CreateUserRequest request) {
        return createUserWithCredential(request, STATUS_INACTIVE, resolveCredentialOwner(request, true));
    }

    public void reinvite(Long id) {
        User user = getByIdOrThrow(id);
        if (!STATUS_ACTIVE.equals(user.getStatus())) {
            user.setStatus(STATUS_INACTIVE);
            userMapper.updateById(user);
        }
    }

    @Transactional
    public User adminResetPassword(String actorRole, Long targetUserId, String newPassword) {
        User target = getByIdOrThrow(targetUserId);
        if ("PLATFORM_ADMIN".equals(actorRole)
                && ("SUPER_ADMIN".equals(target.getPlatformRole()) || "PLATFORM_ADMIN".equals(target.getPlatformRole()))) {
            throw new ForbiddenException("Platform admins cannot reset passwords for admin accounts");
        }
        if (!"SUPER_ADMIN".equals(actorRole) && !"PLATFORM_ADMIN".equals(actorRole)) {
            throw new ForbiddenException("Only platform administrators can reset another user's password");
        }
        target.setPasswordHash(authService.encodePassword(newPassword));
        userMapper.updateById(target);
        return target;
    }

    @Transactional
    public User updatePlatformRole(String actorRole, Long targetUserId, String newPlatformRole) {
        if (!"SUPER_ADMIN".equals(actorRole)) {
            throw new ForbiddenException("Only SUPER_ADMIN can update platform roles");
        }
        String roleCode = requireNonBlank(newPlatformRole, "Platform role must not be blank");
        User target = getByIdOrThrow(targetUserId);
        Long roleId = roleMapper.findIdByCode(roleCode);
        if (roleId == null) {
            throw new ForbiddenException("Unknown platform role: " + roleCode);
        }
        target.setPlatformRole(roleCode);
        target.setRoleId(roleId);
        userMapper.updateById(target);
        return target;
    }

    @Transactional
    protected CreateUserResponse createUserWithCredential(CreateUserRequest request,
                                                          String initialStatus,
                                                          String credentialOwner) {
        User user = buildUser(request, initialStatus);
        userMapper.insert(user);

        CreatePlatformCredentialRequest credentialRequest = new CreatePlatformCredentialRequest(
                user.getId(),
                PERSONAL_CREDENTIAL_TYPE,
                resolveCredentialName(request.credentialName(), credentialOwner),
                request.monthlyTokenQuota(),
                request.alertThresholdPct(),
                request.overQuotaStrategy()
        );
        CreatePlatformCredentialResponse credentialResponse = platformCredentialService.create(credentialRequest);
        return CreateUserResponse.of(user, credentialResponse.plainKey(), credentialResponse.credential());
    }

    private User buildUser(CreateUserRequest request, String initialStatus) {
        String platformRole = resolvePlatformRole(request.platformRole());
        User user = new User();
        user.setEmail(requireNonBlank(request.email(), "Email must not be blank"));
        user.setUsername(requireNonBlank(request.username(), "Username must not be blank"));
        user.setFullName(normalizeText(request.fullName()));
        user.setAvatarUrl(normalizeText(request.avatarUrl()));
        user.setDepartmentId(request.departmentId());
        user.setJobTitle(normalizeText(request.jobTitle()));
        user.setPhone(normalizeText(request.phone()));
        user.setPlatformRole(platformRole);
        user.setRoleId(roleMapper.findIdByCode(platformRole));
        user.setPasswordHash(authService.encodePassword(request.password()));
        user.setStatus(initialStatus);
        return user;
    }

    private void updateEmailIfChanged(Long id, User user, String email) {
        String normalizedEmail = normalizeText(email);
        if (normalizedEmail == null || normalizedEmail.equals(user.getEmail())) {
            return;
        }
        User other = userMapper.selectOne(
                Wrappers.<User>lambdaQuery()
                        .eq(User::getEmail, normalizedEmail)
                        .ne(User::getId, id)
        );
        if (other != null) {
            throw new BusinessException(HttpStatus.CONFLICT.value(), BizErrorCode.CONFLICT, "Email already in use");
        }
        user.setEmail(normalizedEmail);
    }

    private void updateUsernameIfChanged(Long id, User user, String username) {
        String normalizedUsername = normalizeText(username);
        if (normalizedUsername == null || normalizedUsername.equals(user.getUsername())) {
            return;
        }
        User other = userMapper.selectOne(
                Wrappers.<User>lambdaQuery()
                        .eq(User::getUsername, normalizedUsername)
                        .ne(User::getId, id)
        );
        if (other != null) {
            throw new BusinessException(HttpStatus.CONFLICT.value(), BizErrorCode.CONFLICT, "Username already in use");
        }
        user.setUsername(normalizedUsername);
    }

    private void applyTextUpdate(String value, java.util.function.Consumer<String> setter) {
        String normalized = normalizeText(value);
        if (normalized != null) {
            setter.accept(normalized);
        }
    }

    private String resolvePlatformRole(String platformRole) {
        String normalizedRole = normalizeText(platformRole);
        return normalizedRole != null ? normalizedRole : DEFAULT_PLATFORM_ROLE;
    }

    private String resolveCredentialOwner(CreateUserRequest request, boolean fallbackToEmail) {
        String fullName = normalizeText(request.fullName());
        if (fullName != null) {
            return fullName;
        }
        if (!fallbackToEmail) {
            String username = normalizeText(request.username());
            if (username != null) {
                return username;
            }
        }
        return requireNonBlank(request.email(), "Email must not be blank");
    }

    private String resolveCredentialName(String explicitName, String ownerName) {
        String normalized = normalizeText(explicitName);
        if (normalized != null) {
            return normalized;
        }
        return ownerName + DEFAULT_CREDENTIAL_SUFFIX;
    }

    private String requireNonBlank(String value, String message) {
        String normalized = normalizeText(value);
        if (normalized == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(), BizErrorCode.VALIDATION_FAILED, message);
        }
        return normalized;
    }

    private String normalizeText(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
