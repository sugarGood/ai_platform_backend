package com.aiplatform.backend.me;

import com.aiplatform.backend.common.security.AuthContext;
import com.aiplatform.backend.entity.PlatformCredential;
import com.aiplatform.backend.entity.Project;
import com.aiplatform.backend.entity.ProjectMember;
import com.aiplatform.backend.mapper.PlatformCredentialMapper;
import com.aiplatform.backend.mapper.ProjectMapper;
import com.aiplatform.backend.mapper.ProjectMemberMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(statements = {
        "DELETE FROM activity_logs",
        "DELETE FROM key_rotation_logs",
        "DELETE FROM project_member_resource_grants",
        "DELETE FROM project_member_permission_overrides",
        "DELETE FROM project_role_template_permissions",
        "DELETE FROM project_role_templates",
        "DELETE FROM user_client_bindings",
        "DELETE FROM client_apps",
        "DELETE FROM platform_credentials",
        "DELETE FROM project_members",
        "DELETE FROM projects"
}, executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
class MeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private ProjectMemberMapper projectMemberMapper;

    @Autowired
    private PlatformCredentialMapper platformCredentialMapper;

    @AfterEach
    void cleanupAuthContext() {
        AuthContext.clear();
    }

    @Test
    void shouldGetMeCredentialAndUpdateCurrentProject() throws Exception {
        Long userId = 101L;
        Long projectId = insertProject("me-credential-proj");
        insertProjectMember(projectId, userId, "DEVELOPER");
        insertCredential(userId, projectId);

        AuthContext.set(new AuthContext.AuthPrincipal(userId, "u101@test.com", "MEMBER", Set.of()));

        mockMvc.perform(get("/api/me/credential"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userId").value(userId))
                .andExpect(jsonPath("$.data.defaultProject.id").value(projectId));

        mockMvc.perform(put("/api/me/current-project")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"projectId\":null}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.boundProjectId").doesNotExist());
    }

    @Test
    void shouldRotateRenewAndTestCredential() throws Exception {
        Long userId = 102L;
        Long projectId = insertProject("me-ops-proj");
        insertProjectMember(projectId, userId, "DEVELOPER");
        insertCredential(userId, projectId);

        AuthContext.set(new AuthContext.AuthPrincipal(userId, "u102@test.com", "MEMBER", Set.of()));

        mockMvc.perform(post("/api/me/credential/rotate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.plainKey").exists())
                .andExpect(jsonPath("$.data.credential.userId").value(userId));

        mockMvc.perform(post("/api/me/credential/renew")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"renewDays\":30}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userId").value(userId));

        mockMvc.perform(post("/api/me/credential/test")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.success").value(true));
    }

    private Long insertProject(String code) {
        Project project = new Project();
        project.setName("Me Test Project");
        project.setCode(code);
        project.setStatus("ACTIVE");
        projectMapper.insert(project);
        return project.getId();
    }

    private void insertProjectMember(Long projectId, Long userId, String role) {
        ProjectMember member = new ProjectMember();
        member.setProjectId(projectId);
        member.setUserId(userId);
        member.setRole(role);
        projectMemberMapper.insert(member);
    }

    private void insertCredential(Long userId, Long projectId) {
        PlatformCredential credential = new PlatformCredential();
        credential.setUserId(userId);
        credential.setCredentialType("PERSONAL");
        credential.setName("Personal Credential");
        credential.setKeyPrefix("plt_test");
        credential.setKeyHash("hash");
        credential.setStatus("ACTIVE");
        credential.setBoundProjectId(projectId);
        credential.setMonthlyTokenQuota(200000L);
        credential.setUsedTokensThisMonth(0L);
        credential.setAlertThresholdPct(80);
        credential.setOverQuotaStrategy("BLOCK");
        credential.setLastQuotaResetAt(LocalDateTime.now());
        platformCredentialMapper.insert(credential);
    }
}
