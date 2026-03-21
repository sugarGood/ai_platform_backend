package com.aiplatform.backend.service;

import com.aiplatform.backend.common.exception.UserNotFoundException;
import com.aiplatform.backend.dto.InviteUserRequest;
import com.aiplatform.backend.dto.UpdateUserRequest;
import com.aiplatform.backend.entity.User;
import com.aiplatform.backend.mapper.UserMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户业务服务。
 * <p>处理用户的邀请、信息更新、查询等操作。</p>
 */
@Service
public class UserService {

    private final UserMapper userMapper;

    /**
     * 构造方法，注入用户数据访问层。
     *
     * @param userMapper 用户 Mapper
     */
    public UserService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    /**
     * 邀请新用户加入平台。
     * <p>根据请求参数创建用户记录，默认角色为 MEMBER，状态为 ACTIVE。</p>
     *
     * @param request 邀请用户请求参数
     * @return 新创建的用户实体
     */
    public User invite(InviteUserRequest request) {
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
        return user;
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
