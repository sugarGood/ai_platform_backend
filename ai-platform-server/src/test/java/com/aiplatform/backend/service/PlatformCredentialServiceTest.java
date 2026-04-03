package com.aiplatform.backend.service;

import com.aiplatform.backend.entity.ActivityLog;
import com.aiplatform.backend.entity.KeyRotationLog;
import com.aiplatform.backend.entity.PlatformCredential;
import com.aiplatform.backend.mapper.ActivityLogMapper;
import com.aiplatform.backend.mapper.KeyRotationLogMapper;
import com.aiplatform.backend.mapper.PlatformCredentialMapper;
import com.aiplatform.backend.mapper.ProjectMapper;
import com.aiplatform.backend.mapper.ProjectMemberMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlatformCredentialServiceTest {

    @Mock
    private PlatformCredentialMapper platformCredentialMapper;
    @Mock
    private KeyRotationLogMapper keyRotationLogMapper;
    @Mock
    private ProjectMemberMapper projectMemberMapper;
    @Mock
    private ProjectMapper projectMapper;
    @Mock
    private ActivityLogMapper activityLogMapper;

    @InjectMocks
    private PlatformCredentialService platformCredentialService;

    @Test
    void renewByUserIdShouldWriteRotationLogAndActivityLog() {
        PlatformCredential credential = new PlatformCredential();
        credential.setId(7L);
        credential.setUserId(12L);
        credential.setBoundProjectId(3L);
        credential.setName("Alice Credential");
        credential.setKeyPrefix("plt_12_abcd");
        credential.setStatus("ACTIVE");

        when(platformCredentialMapper.selectOne(any())).thenReturn(credential);
        when(platformCredentialMapper.selectById(7L)).thenReturn(credential);

        platformCredentialService.renewByUserId(12L, 30);

        ArgumentCaptor<PlatformCredential> credentialCaptor = ArgumentCaptor.forClass(PlatformCredential.class);
        verify(platformCredentialMapper).updateById(credentialCaptor.capture());
        PlatformCredential updated = credentialCaptor.getValue();
        assertEquals(7L, updated.getId());
        assertTrue(updated.getExpiresAt().isAfter(LocalDateTime.now().minusDays(1)));

        ArgumentCaptor<KeyRotationLog> rotationLogCaptor = ArgumentCaptor.forClass(KeyRotationLog.class);
        verify(keyRotationLogMapper).insert(rotationLogCaptor.capture());
        KeyRotationLog rotationLog = rotationLogCaptor.getValue();
        assertEquals("PLATFORM_CREDENTIAL", rotationLog.getTargetType());
        assertEquals(7L, rotationLog.getTargetId());
        assertEquals("RENEW", rotationLog.getRotationType());
        assertEquals("SUCCESS", rotationLog.getResult());
        assertEquals(12L, rotationLog.getOperatedBy());

        ArgumentCaptor<ActivityLog> activityLogCaptor = ArgumentCaptor.forClass(ActivityLog.class);
        verify(activityLogMapper).insert(activityLogCaptor.capture());
        ActivityLog activityLog = activityLogCaptor.getValue();
        assertEquals(3L, activityLog.getProjectId());
        assertEquals(12L, activityLog.getUserId());
        assertEquals("credential.renew", activityLog.getActionType());
        assertEquals("platform_credential", activityLog.getTargetType());
        assertEquals(7L, activityLog.getTargetId());
    }
}
