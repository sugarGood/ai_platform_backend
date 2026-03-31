package com.aiplatform.backend.service;

import com.aiplatform.backend.entity.MemberAiQuota;
import com.aiplatform.backend.entity.Project;
import com.aiplatform.backend.entity.ProjectMember;
import com.aiplatform.backend.mapper.AiUsageEventMapper;
import com.aiplatform.backend.mapper.AlertEventMapper;
import com.aiplatform.backend.mapper.MemberAiQuotaMapper;
import com.aiplatform.backend.mapper.ProjectMemberMapper;
import com.aiplatform.backend.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectTokenDashboardServiceTest {

    @Mock
    private ProjectService projectService;
    @Mock
    private ProjectMemberService projectMemberService;
    @Mock
    private ProjectMemberMapper projectMemberMapper;
    @Mock
    private UserMapper userMapper;
    @Mock
    private PlatformCredentialService platformCredentialService;
    @Mock
    private MemberAiQuotaMapper memberAiQuotaMapper;
    @Mock
    private AiUsageEventMapper aiUsageEventMapper;
    @Mock
    private AlertEventMapper alertEventMapper;

    @InjectMocks
    private ProjectTokenDashboardService projectTokenDashboardService;

    @Test
    void syncMemberQuotasShouldTrimRoleAndFallbackToMonthlyCycle() {
        Project project = new Project();
        project.setId(5L);
        project.setQuotaResetCycle("   ");
        when(projectService.getByIdOrThrow(5L)).thenReturn(project);

        ProjectMember admin = new ProjectMember();
        admin.setProjectId(5L);
        admin.setUserId(11L);
        admin.setRole(" ADMIN ");
        when(projectMemberMapper.selectList(any())).thenReturn(List.of(admin));
        when(memberAiQuotaMapper.selectOne(any())).thenReturn(null);

        int created = projectTokenDashboardService.syncMemberQuotas(5L);

        assertEquals(1, created);
        ArgumentCaptor<MemberAiQuota> captor = ArgumentCaptor.forClass(MemberAiQuota.class);
        verify(memberAiQuotaMapper, times(1)).insert(captor.capture());
        MemberAiQuota quota = captor.getValue();
        assertEquals(300_000L, quota.getQuotaLimit());
        assertEquals("MONTHLY", quota.getResetCycle());
        assertEquals("ACTIVE", quota.getStatus());
    }
}
