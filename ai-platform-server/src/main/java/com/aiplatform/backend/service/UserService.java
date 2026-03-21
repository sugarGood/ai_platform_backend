package com.aiplatform.backend.service;

import com.aiplatform.backend.common.exception.UserNotFoundException;
import com.aiplatform.backend.dto.CreatePlatformCredentialRequest;
import com.aiplatform.backend.dto.CreatePlatformCredentialResponse;
import com.aiplatform.backend.dto.CreateUserRequest;
import com.aiplatform.backend.dto.CreateUserResponse;
import com.aiplatform.backend.dto.UpdateUserRequest;
import com.aiplatform.backend.entity.User;
import com.aiplatform.backend.mapper.UserMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
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

    public UserService(UserMapper userMapper, PlatformCredentialService platformCredentialService) {
        this.userMapper = userMapper;
        this.platformCredentialService = platformCredentialService;
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
        user.setDepartmentId(request.departmentId());
        user.setJobTitle(request.jobTitle());
        user.setPhone(request.phone());
        user.setPlatformRole(request.platformRole() != null ? request.platformRole() : "MEMBER");
        user.setStatus("ACTIVE");
        userMapper.insert(user);

        // 2. 自动分配平台凭证（一人一证，跨项目共用）
        String credentialName = (user.getFullName() != null ? user.getFullName() : user.getUsername()) + " 的凭证";
        CreatePlatformCredentialRequest credReq = new CreatePlatformCredentialRequest(
                user.getId(),
                "PERSONAL",
                credentialName,
                null,   // monthlyTokenQuota: 使用默认值 200K
                null,   // alertThresholdPct: 使用默认值 80
                null    // overQuotaStrategy: 使用默认值 BLOCK
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
        userMapper.updateById(user);
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
     * 根据邮箱查询用户。
     *
     * @param email 邮箱地址
     * @return 匹配的用户实体，若不存在返回 {@code null}
     */
    public User findByEmail(String email) {
        return userMapper.selectOne(Wrappers.<User>lambdaQuery().eq(User::getEmail, email));
    }
}
