package com.aiplatform.backend.service;

import com.aiplatform.backend.dto.UpdateProjectAgentRequest;
import com.aiplatform.backend.entity.ProjectAgent;
import com.aiplatform.backend.mapper.ProjectAgentMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectAgentServiceTest {

    @Mock
    private ProjectAgentMapper projectAgentMapper;

    @InjectMocks
    private ProjectAgentService projectAgentService;

    @Test
    void updateShouldTrimTextFieldsAndIgnoreBlankName() {
        ProjectAgent existing = new ProjectAgent();
        existing.setId(1L);
        existing.setProjectId(9L);
        existing.setName("Existing Agent");
        existing.setDescription("old");
        existing.setStatus("ACTIVE");
        existing.setEnableDeploy(false);
        when(projectAgentMapper.selectOne(any())).thenReturn(existing);

        UpdateProjectAgentRequest request = new UpdateProjectAgentRequest(
                "   ",
                " updated description ",
                null,
                null,
                null,
                null,
                null,
                null,
                true,
                null,
                " DISABLED "
        );

        projectAgentService.update(9L, request);

        ArgumentCaptor<ProjectAgent> captor = ArgumentCaptor.forClass(ProjectAgent.class);
        verify(projectAgentMapper).updateById(captor.capture());
        ProjectAgent updated = captor.getValue();
        assertEquals("Existing Agent", updated.getName());
        assertEquals("updated description", updated.getDescription());
        assertTrue(updated.getEnableDeploy());
        assertEquals("DISABLED", updated.getStatus());
    }

    @Test
    void initForProjectShouldDisableDeployAndMonitoringByDefault() {
        var project = new com.aiplatform.backend.entity.Project();
        project.setId(3L);
        project.setName("Ops");
        project.setCode("ops");
        project.setProjectType("PLATFORM");

        ProjectAgent agent = projectAgentService.initForProject(project, 7L);

        assertFalse(agent.getEnableDeploy());
        assertFalse(agent.getEnableMonitoring());
    }
}
