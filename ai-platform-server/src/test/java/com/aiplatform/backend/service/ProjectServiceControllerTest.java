package com.aiplatform.backend.service;

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
        "DELETE FROM services",
        "DELETE FROM projects"
}, executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
class ProjectServiceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCreateServiceUnderProject() throws Exception {
        Long projectId = createProject("mall-service-create");

        mockMvc.perform(post("/api/projects/{projectId}/services", projectId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "name": "mall-backend"
                        }
                        """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.projectId").value(projectId))
            .andExpect(jsonPath("$.name").value("mall-backend"))
            .andExpect(jsonPath("$.mainBranch").value("main"));
    }

    @Test
    void shouldListServicesUnderProject() throws Exception {
        Long projectId = createProject("mall-service-list");

        mockMvc.perform(post("/api/projects/{projectId}/services", projectId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "name": "mall-frontend",
                          "language": "TypeScript",
                          "framework": "Vue"
                        }
                        """))
            .andExpect(status().isCreated());

        mockMvc.perform(get("/api/projects/{projectId}/services", projectId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].projectId").value(projectId))
            .andExpect(jsonPath("$[0].name").value("mall-frontend"));
    }

    @Test
    void shouldReturnNotFoundWhenProjectMissing() throws Exception {
        mockMvc.perform(post("/api/projects/999/services")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "name": "mall-backend"
                        }
                        """))
            .andExpect(status().isNotFound());
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
