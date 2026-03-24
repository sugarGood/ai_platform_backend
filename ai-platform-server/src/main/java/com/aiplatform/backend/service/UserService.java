package com.aiplatform.backend.service;

import com.aiplatform.backend.common.exception.BusinessException;
import com.aiplatform.backend.common.exception.BizErrorCode;
import com.aiplatform.backend.common.exception.ForbiddenException;
import com.aiplatform.backend.common.exception.UserNotFoundException;
import com.aiplatform.backend.dto.CreatePlatformCredentialRequest;
import com.aiplatform.backend.dto.CreatePlatformCredentialResponse;
import com.aiplatform.backend.dto.CreateUserRequest;
import com.aiplatform.backend.dto.CreateUserResponse;
import com.aiplatform.backend.dto.UpdateUserRequest;
import com.aiplatform.backend.entity.User;
import com.aiplatform.backend.mapper.RoleMapper;
import com.aiplatform.backend.mapper.UserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 用户业务服务。
 * <p>处理用户的新增、信息更新、查询等操作。</p>
 */
@Service
public class UserService {

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

    /**
     * 新增用户，并自动分配平台凭证（一人一证）。
     *
     * <p>在同一事务中完成：
     * <ol>
     *   <li>插入用户记录</li>
     *   <li>生成并插入平台凭证（个人月度 Token 配额默认 200K）</li>
     * </ol>
     * 返回的 {@code credentialPlainKey} 为凭证明文密钥，<b>仅此一次</b>，请妥善告知用户保存。</p>
     *
     * @param request 新增用户请求参数
     * @return 包含用户信息和凭证明文密钥的响应
     */
    @Transactional
    public CreateUserResponse create(CreateUserRequest request) {
        // 1. 创建用户
        User user = new User();
        user.setEmail(request.email());
        user.setUsername(request.username());
        user.setFullName(request.fullName());
        if (request.avatarUrl() != null && !request.avatarUrl().isBlank()) {
            user.setAvatarUrl(request.avatarUrl().trim());
        }
        user.setDepartmentId(request.departmentId());
        user.setJobTitle(request.jobTitle());
        user.setPhone(request.phone());
        String roleCode = request.platformRole() != null ? request.platformRole() : "MEMBER";
        user.setPlatformRole(roleCode);
        user.setRoleId(roleMapper.findIdByCode(roleCode));
        user.setPasswordHash(authService.encodePassword(request.password()));
        user.setStatus("ACTIVE");
        userMapper.insert(user);

        // 2. 自动分配平台凭证（一人一证，跨项目共用）
        String defaultCredName = (user.getFullName() != null ? user.getFullName() : user.getUsername()) + " 的凭证";
        String credentialName = request.credentialName() != null && !request.credentialName().isBlank()
                ? request.credentialName().trim()
                : defaultCredName;
        CreatePlatformCredentialRequest credReq = new CreatePlatformCredentialRequest(
                user.getId(),
                "PERSONAL",
                credentialName,
                request.monthlyTokenQuota(),
                request.alertThresholdPct(),
                request.overQuotaStrategy()
        );
        CreatePlatformCredentialResponse credResp = platformCredentialService.create(credReq);

        return CreateUserResponse.of(user, credResp.plainKey(), credResp.credential());
    }

    /**
     * 更新用户信息。
     * <p>仅更新请求中非空的字段，其余字段保持不变。</p>
     *
     * @param id      用户ID
     * @param request 更新请求参数
     * @return 更新后的用户实体
     * @throws UserNotFoundException 当用户不存在时抛出
     */
    public User update(Long id, UpdateUserRequest request) {
        User user = getByIdOrThrow(id);
        if (request.fullName() != null) {
            user.setFullName(request.fullName());
        }
        if (request.avatarUrl() != null) {
            user.setAvatarUrl(request.avatarUrl());
        }
        if (request.departmentId() != null) {
            user.setDepartmentId(request.departmentId());
        }
        if (request.jobTitle() != null) {
            user.setJobTitle(request.jobTitle());
        }
        if (request.phone() != null) {
            user.setPhone(request.phone());
        }
        if (request.email() != null) {
            String e = request.email().trim();
            if (!e.isEmpty() && !e.equals(user.getEmail())) {
                User other = userMapper.selectOne(
                        Wrappers.<User>lambdaQuery().eq(User::getEmail, e).ne(User::getId, id));
                if (other != null) {
                    throw new BusinessException(
                            HttpStatus.CONFLICT.value(), BizErrorCode.CONFLICT, "邮箱已被使用");
                }
                user.setEmail(e);
            }
        }
        if (request.username() != null) {
            String u = request.username().trim();
            if (!u.isEmpty() && !u.equals(user.getUsername())) {
                User other = userMapper.selectOne(
                        Wrappers.<User>lambdaQuery().eq(User::getUsername, u).ne(User::getId, id));
                if (other != null) {
                    throw new BusinessException(
                            HttpStatus.CONFLICT.value(), BizErrorCode.CONFLICT, "用户名已被使用");
                }
                user.setUsername(u);
            }
        }
        userMapper.updateById(user);

        platformCredentialService.patchPersonalCredentialForUser(
                id,
                request.monthlyTokenQuota(),
                request.alertThresholdPct(),
                request.overQuotaStrategy(),
                request.credentialName());

        return user;
    }

    /**
     * 根据ID查询用户，若不存在则抛出异常。
     *
     * @param id 用户ID
     * @return 用户实体
     * @throws UserNotFoundException 当用户不存在时抛出
     */
    public User getByIdOrThrow(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new UserNotFoundException(id);
        }
        return user;
    }

    /**
     * 查询全部用户列表，按ID升序排列。
     *
     * @return 用户列表
     */
    public List<User> list() {
        return userMapper.selectList(Wrappers.<User>lambdaQuery().orderByAsc(User::getId));
    }

    /**
     * 按关键词、部门、角色、状态筛选用户列表。
     *
     * @param keyword      搜索关键词（模糊匹配姓名/邮箱/用户名），可为 null
     * @param departmentId 部门ID，可为 null
     * @param platformRole 平台角色，可为 null
     * @param status       账号状态，可为 null
     * @return 匹配的用户列表
     */
    public List<User> search(String keyword, Long departmentId, String platformRole, String status) {
        LambdaQueryWrapper<User> query = Wrappers.<User>lambdaQuery();
        if (keyword != null && !keyword.isBlank()) {
            query.and(w -> w.like(User::getFullName, keyword)
                    .or().like(User::getEmail, keyword)
                    .or().like(User::getUsername, keyword));
        }
        if (departmentId != null) {
            query.eq(User::getDepartmentId, departmentId);
        }
        if (platformRole != null && !platformRole.isBlank()) {
            query.eq(User::getPlatformRole, platformRole);
        }
        if (status != null && !status.isBlank()) {
            query.eq(User::getStatus, status);
        }
        query.orderByAsc(User::getId);
        return userMapper.selectList(query);
    }

    /**
     * 切换用户账号状态（ACTIVE ↔ DISABLED）。
     *
     * <p>停用账号时同步吊销该用户的所有有效平台凭证。</p>
     *
     * @param id        用户ID
     * @param newStatus 新状态（ACTIVE / DISABLED）
     * @return 更新后的用户实体
     * @throws UserNotFoundException 当用户不存在时抛出
     */
    @Transactional
    public User updateStatus(Long id, String newStatus) {
        User user = getByIdOrThrow(id);
        user.setStatus(newStatus);
        userMapper.updateById(user);
        // 停用账号时联动吊销凭证
        if ("DISABLED".equals(newStatus)) {
            platformCredentialService.revokeAllByUserId(id, "账号已停用，自动吊销凭证");
        }
        return user;
    }

    /**
     * 根据邮箱查询用户。
     *
     * @param email 邮箱地址
     * @return 匹配的用户实体，若不存在返回 {@code null}
     */
    public User findByEmail(String email) {
        return userMapper.selectOne(Wrappers.<User>lambdaQuery().eq(User::getEmail, email));
    }

    /**
     * 邀请用户：创建 INACTIVE 状态的用户并自动分配凭证。
     * 实际邮件发送可接入消息队列或邮件服务，此处记录邀请状态。
     *
     * @param request 邀请请求参数
     * @return 包含用户信息和凭证明文密钥的响应
     */
    @Transactional
    public CreateUserResponse invite(CreateUserRequest request) {
        User user = new User();
        user.setEmail(request.email());
        user.setUsername(request.username());
        user.setFullName(request.fullName());
        if (request.avatarUrl() != null && !request.avatarUrl().isBlank()) {
            user.setAvatarUrl(request.avatarUrl().trim());
        }
        user.setDepartmentId(request.departmentId());
        user.setJobTitle(request.jobTitle());
        user.setPhone(request.phone());
        String inviteRoleCode = request.platformRole() != null ? request.platformRole() : "MEMBER";
        user.setPlatformRole(inviteRoleCode);
        user.setRoleId(roleMapper.findIdByCode(inviteRoleCode));
        user.setPasswordHash(authService.encodePassword(request.password()));
        user.setStatus("INACTIVE");
        userMapper.insert(user);

        String defaultCredName = (user.getFullName() != null ? user.getFullName() : user.getEmail()) + " 的凭证";
        String credentialName = request.credentialName() != null && !request.credentialName().isBlank()
                ? request.credentialName().trim()
                : defaultCredName;
        CreatePlatformCredentialRequest credReq = new CreatePlatformCredentialRequest(
                user.getId(),
                "PERSONAL",
                credentialName,
                request.monthlyTokenQuota(),
                request.alertThresholdPct(),
                request.overQuotaStrategy());
        CreatePlatformCredentialResponse credResp = platformCredentialService.create(credReq);
        return CreateUserResponse.of(user, credResp.plainKey(), credResp.credential());
    }

    /**
     * 重发邀请邮件（将用户状态重置为 INACTIVE，触发邮件通知）。
     *
     * @param id 用户ID
     */
    public void reinvite(Long id) {
        User user = getByIdOrThrow(id);
        // 实际项目中此处发送邀请邮件，此处仅重置状态
        if (!"ACTIVE".equals(user.getStatus())) {
            user.setStatus("INACTIVE");
            userMapper.updateById(user);
        }
        // TODO: 集成邮件服务发送邀请链接
    }

    /**
     * 管理员重置指定用户的登录密码（无需原密码）。
     *
     * <p>规则：{@code SUPER_ADMIN} 可重置任意用户；{@code PLATFORM_ADMIN} 仅可重置 {@code MEMBER}，
     * 不可重置 {@code PLATFORM_ADMIN} / {@code SUPER_ADMIN}。</p>
     *
     * @param actorRole    操作者平台角色
     * @param targetUserId 目标用户 ID
     * @param newPassword  新明文密码
     * @return 更新后的用户实体
     */
    @Transactional
    public User adminResetPassword(String actorRole, Long targetUserId, String newPassword) {
        User target = getByIdOrThrow(targetUserId);
        if ("PLATFORM_ADMIN".equals(actorRole)) {
            if ("SUPER_ADMIN".equals(target.getPlatformRole()) || "PLATFORM_ADMIN".equals(target.getPlatformRole())) {
                throw new ForbiddenException("平台管理员不可重置超级管理员或其它平台管理员的密码");
            }
        } else if (!"SUPER_ADMIN".equals(actorRole)) {
            throw new ForbiddenException("无权重置他人密码");
        }
        target.setPasswordHash(authService.encodePassword(newPassword));
        userMapper.updateById(target);
        return target;
    }

    /**
     * 更新用户平台角色（同步写入 {@code role_id}）。
     *
     * <p>仅 {@code SUPER_ADMIN} 可调用，目标角色须为 {@code roles} 表中 {@code role_scope = PLATFORM}
     * 且已启用的编码。</p>
     *
     * @param actorRole       操作者平台角色
     * @param targetUserId    目标用户 ID
     * @param newPlatformRole 新角色编码（如 MEMBER、PLATFORM_ADMIN、SUPER_ADMIN）
     * @return 更新后的用户实体
     */
    @Transactional
    public User updatePlatformRole(String actorRole, Long targetUserId, String newPlatformRole) {
        if (!"SUPER_ADMIN".equals(actorRole)) {
            throw new ForbiddenException("仅超级管理员可修改用户平台角色");
        }
        if (newPlatformRole == null || newPlatformRole.isBlank()) {
            throw new ForbiddenException("平台角色不能为空");
        }
        String code = newPlatformRole.trim();
        User target = getByIdOrThrow(targetUserId);
        Long roleId = roleMapper.findIdByCode(code);
        if (roleId == null) {
            throw new ForbiddenException("平台角色不存在或未启用: " + code);
        }
        target.setPlatformRole(code);
        target.setRoleId(roleId);
        userMapper.updateById(target);
        return target;
    }
}
