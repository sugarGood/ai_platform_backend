package com.aiplatform.backend.member;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(statements = {
        "DELETE FROM project_members",
        "DELETE FROM services",
        "DELETE FROM projects"
}, executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
class ProjectMemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCreateMemberUnderProject() throws Exception {
        Long projectId = createProject("mall-member-create");

        mockMvc.perform(post("/api/projects/{projectId}/members", projectId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "userId": 1,
                          "role": "ADMIN"
                        }
                        """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.projectId").value(projectId))
            .andExpect(jsonPath("$.userId").value(1))
            .andExpect(jsonPath("$.role").value("ADMIN"))
            .andExpect(jsonPath("$.credentialStatus").exists())
            .andExpect(jsonPath("$.credentialExpiresInDays").exists())
            .andExpect(jsonPath("$.credentialExpiresAt").exists());
    }

    @Test
    void shouldListMembersUnderProject() throws Exception {
        Long projectId = createProject("mall-member-list");

        mockMvc.perform(post("/api/projects/{projectId}/members", projectId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "userId": 2,
                          "role": "DEVELOPER"
                        }
                        """))
            .andExpect(status().isCreated());

        mockMvc.perform(get("/api/projects/{projectId}/members", projectId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].userId").value(2))
            .andExpect(jsonPath("$[0].role").value("DEVELOPER"))
            .andExpect(jsonPath("$[0].credentialStatus").exists());
    }

    @Test
    void shouldReturnNotFoundWhenProjectMissing() throws Exception {
        mockMvc.perform(post("/api/projects/999/members")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "userId": 1,
                          "role": "ADMIN"
                        }
                        """))
            .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnConflictWhenMemberDuplicated() throws Exception {
        Long projectId = createProject("mall-member-duplicate");

        mockMvc.perform(post("/api/projects/{projectId}/members", projectId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "userId": 3,
                          "role": "ADMIN"
                        }
                        """))
            .andExpect(status().isCreated());

        mockMvc.perform(post("/api/projects/{projectId}/members", projectId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "userId": 3,
                          "role": "DEVELOPER"
                        }
                        """))
            .andExpect(status().isConflict());
    }

    private Long createProject(String code) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/projects")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "name": "Mall System",
                          "code": "%s",
                          "projectType": "PRODUCT"
                        }
                        """.formatted(code)))
            .andExpect(status().isCreated())
            .andReturn();

        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
        return response.get("id").asLong();
    }
}
