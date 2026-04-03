package com.aiplatform.backend.project;

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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(statements = "DELETE FROM projects", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldCreateProject() throws Exception {
        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "商城系统",
                                  "code": "mall",
                                  "projectType": "PRODUCT"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name").value("商城系统"))
                .andExpect(jsonPath("$.data.code").value("mall"))
                .andExpect(jsonPath("$.data.projectType").value("PRODUCT"))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));
    }

    @Test
    void shouldListProjectsPaged() throws Exception {
        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "用户中心",
                                  "code": "user-center",
                                  "projectType": "PLATFORM"
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/projects?page=1&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.data[0].code").value("user-center"));
    }

    @Test
    void shouldGetProjectById() throws Exception {
        var result = mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "数据平台",
                                  "code": "data-platform",
                                  "projectType": "DATA"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn();

        String body = result.getResponse().getContentAsString();
        Long id = ObjectMapper.class.getDeclaredConstructor()
                .newInstance()
                .readTree(body)
                .path("data")
                .path("id")
                .asLong();

        mockMvc.perform(get("/api/projects/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.code").value("data-platform"));
    }

    @Test
    void shouldRejectBlankProjectName() throws Exception {
        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "",
                                  "code": "bad-project",
                                  "projectType": "PRODUCT"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnNotFoundForMissingProject() throws Exception {
        mockMvc.perform(get("/api/projects/999"))
                .andExpect(status().isNotFound());
    }
}
