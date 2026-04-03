package com.aiplatform.backend.service;

import com.aiplatform.backend.dto.CreatePlatformCredentialRequest;
import com.aiplatform.backend.dto.CreatePlatformCredentialResponse;
import com.aiplatform.backend.dto.CreateUserRequest;
import com.aiplatform.backend.dto.PlatformCredentialResponse;
import com.aiplatform.backend.entity.User;
import com.aiplatform.backend.mapper.RoleMapper;
import com.aiplatform.backend.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserMapper userMapper;
    @Mock
    private PlatformCredentialService platformCredentialService;
    @Mock
    private AuthService authService;
    @Mock
    private RoleMapper roleMapper;

    @InjectMocks
    private UserService userService;

    @Test
    void createShouldDefaultBlankRoleAndCredentialName() {
        when(authService.encodePassword("password123")).thenReturn("hashed-password");
        when(roleMapper.findIdByCode("MEMBER")).thenReturn(3L);
        when(platformCredentialService.create(any())).thenReturn(new CreatePlatformCredentialResponse(
                "plain-key",
                new PlatformCredentialResponse(1L, 12L, null, "PERSONAL", "plt_", "Alice Credential",
                        200_000L, 0L, 80, "BLOCK", "ACTIVE", null, null, null)
        ));
        doAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(12L);
            return 1;
        }).when(userMapper).insert(any(User.class));

        CreateUserRequest request = new CreateUserRequest(
                "alice@example.com",
                "alice",
                "password123",
                "Alice",
                null,
                null,
                null,
                null,
                "   ",
                "   ",
                200_000L,
                80,
                "BLOCK"
        );

        userService.create(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userMapper).insert(userCaptor.capture());
        User created = userCaptor.getValue();
        assertEquals("MEMBER", created.getPlatformRole());
        assertEquals(3L, created.getRoleId());
        assertEquals("ACTIVE", created.getStatus());

        ArgumentCaptor<CreatePlatformCredentialRequest> credentialCaptor =
                ArgumentCaptor.forClass(CreatePlatformCredentialRequest.class);
        verify(platformCredentialService).create(credentialCaptor.capture());
        CreatePlatformCredentialRequest credentialRequest = credentialCaptor.getValue();
        assertEquals(12L, credentialRequest.userId());
        assertEquals("PERSONAL", credentialRequest.credentialType());
        assertEquals("Alice Credential", credentialRequest.name());
    }

    @Test
    void inviteShouldCreateInactiveUserAndFallbackCredentialNameToEmail() {
        when(authService.encodePassword("password123")).thenReturn("hashed-password");
        when(roleMapper.findIdByCode("MEMBER")).thenReturn(4L);
        when(platformCredentialService.create(any())).thenReturn(new CreatePlatformCredentialResponse(
                "plain-key",
                new PlatformCredentialResponse(2L, 18L, null, "PERSONAL", "plt_", "bob@example.com Credential",
                        null, 0L, 80, "BLOCK", "ACTIVE", null, null, null)
        ));
        doAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(18L);
            return 1;
        }).when(userMapper).insert(any(User.class));

        CreateUserRequest request = new CreateUserRequest(
                "bob@example.com",
                "bob",
                "password123",
                null,
                null,
                null,
                null,
                null,
                null,
                " ",
                null,
                null,
                null
        );

        userService.invite(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userMapper).insert(userCaptor.capture());
        User invited = userCaptor.getValue();
        assertEquals("INACTIVE", invited.getStatus());
        assertEquals("MEMBER", invited.getPlatformRole());
        assertEquals(4L, invited.getRoleId());
        assertNull(invited.getFullName());

        ArgumentCaptor<CreatePlatformCredentialRequest> credentialCaptor =
                ArgumentCaptor.forClass(CreatePlatformCredentialRequest.class);
        verify(platformCredentialService).create(credentialCaptor.capture());
        assertEquals("bob@example.com Credential", credentialCaptor.getValue().name());
    }

    @Test
    void updateStatusShouldTrimInputAndRevokeCredentialsWhenDisabled() {
        User user = new User();
        user.setId(9L);
        user.setStatus("ACTIVE");
        when(userMapper.selectById(9L)).thenReturn(user);

        userService.updateStatus(9L, " DISABLED ");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userMapper).updateById(userCaptor.capture());
        assertEquals("DISABLED", userCaptor.getValue().getStatus());
        verify(platformCredentialService).revokeAllByUserId(eq(9L), eq("Account disabled, credentials revoked automatically"));
    }
}
