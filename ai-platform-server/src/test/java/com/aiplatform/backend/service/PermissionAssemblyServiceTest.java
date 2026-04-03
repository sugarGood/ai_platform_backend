package com.aiplatform.backend.service;

import com.aiplatform.backend.dto.ProjectMemberPermissionOverridesResponse;
import com.aiplatform.backend.entity.PlatformCredential;
import com.aiplatform.backend.entity.Project;
import com.aiplatform.backend.entity.ProjectMember;
import com.aiplatform.backend.entity.ProjectMemberPermissionOverride;
import com.aiplatform.backend.entity.ProjectMemberResourceGrant;
import com.aiplatform.backend.entity.ProjectRoleTemplate;
import com.aiplatform.backend.entity.ProjectRoleTemplatePermission;
import com.aiplatform.backend.mapper.ProjectMemberPermissionOverrideMapper;
import com.aiplatform.backend.mapper.ProjectMemberResourceGrantMapper;
import com.aiplatform.backend.mapper.ProjectRoleTemplateMapper;
import com.aiplatform.backend.mapper.ProjectRoleTemplatePermissionMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Sql(statements = {
        "DELETE FROM project_member_resource_grants",
        "DELETE FROM project_member_permission_overrides",
        "DELETE FROM project_role_template_permissions",
        "DELETE FROM project_role_templates"
}, executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
class PermissionAssemblyServiceTest {

    @Autowired
    private PermissionAssemblyService permissionAssemblyService;

    @Autowired
    private ProjectRoleTemplateMapper projectRoleTemplateMapper;

    @Autowired
    private ProjectRoleTemplatePermissionMapper projectRoleTemplatePermissionMapper;

    @Autowired
    private ProjectMemberPermissionOverrideMapper projectMemberPermissionOverrideMapper;

    @Autowired
    private ProjectMemberResourceGrantMapper projectMemberResourceGrantMapper;

    @Test
    void shouldDenyWhenRoleAllowsButMemberOverrideDeny() {
        long projectId = 1001L;
        long memberId = 5001L;
        long userId = 9001L;

        Long templateId = insertRoleTemplate(projectId, "DEVELOPER", "ACTIVE");
        insertRolePermission(templateId, "TOOL", "FULL_CONTROL");

        ProjectMemberPermissionOverride override = new ProjectMemberPermissionOverride();
        override.setProjectMemberId(memberId);
        override.setModuleKey("TOOL");
        override.setAccessLevel("NONE");
        projectMemberPermissionOverrideMapper.insert(override);

        Project project = new Project();
        project.setId(projectId);
        project.setStatus("ACTIVE");

        ProjectMember member = new ProjectMember();
        member.setId(memberId);
        member.setProjectId(projectId);
        member.setUserId(userId);
        member.setRole("DEVELOPER");

        PlatformCredential credential = new PlatformCredential();
        credential.setId(7001L);
        credential.setStatus("ACTIVE");

        ProjectMemberPermissionOverridesResponse response = permissionAssemblyService.assemble(
                project,
                member,
                credential,
                List.of(),
                List.of(),
                List.of(11L, 12L),
                List.of(),
                List.of()
        );

        assertThat(response.effectiveSummary().toolAccessSummary().accessLevel()).isEqualTo("NONE");
        assertThat(response.effectiveSummary().toolAccessSummary().accessibleCount()).isEqualTo(0);
    }

    @Test
    void shouldNotExposeResourceWhenNoGrantEvenIfAccessAllowed() {
        long projectId = 1002L;
        long memberId = 5002L;
        long userId = 9002L;

        Long templateId = insertRoleTemplate(projectId, "DEVELOPER", "ACTIVE");
        insertRolePermission(templateId, "SKILL", "FULL_CONTROL");

        ProjectMemberResourceGrant grant = new ProjectMemberResourceGrant();
        grant.setProjectMemberId(memberId);
        grant.setResourceType("SKILL");
        grant.setResourceId(21L);
        grant.setGrantLevel("ALLOW");
        projectMemberResourceGrantMapper.insert(grant);

        Project project = new Project();
        project.setId(projectId);
        project.setStatus("ACTIVE");

        ProjectMember member = new ProjectMember();
        member.setId(memberId);
        member.setProjectId(projectId);
        member.setUserId(userId);
        member.setRole("DEVELOPER");

        PlatformCredential credential = new PlatformCredential();
        credential.setId(7002L);
        credential.setStatus("ACTIVE");

        ProjectMemberPermissionOverridesResponse response = permissionAssemblyService.assemble(
                project,
                member,
                credential,
                List.of(),
                List.of(21L, 22L),
                List.of(),
                List.of(),
                List.of()
        );

        assertThat(response.effectiveSummary().skillAccessSummary().scopeMode()).isEqualTo("SELECTED");
        assertThat(response.effectiveSummary().skillAccessSummary().selectedResourceIds()).containsExactly(21L);
        assertThat(response.effectiveSummary().skillAccessSummary().selectedResourceIds()).doesNotContain(22L);
    }

    private Long insertRoleTemplate(Long projectId, String roleCode, String status) {
        ProjectRoleTemplate template = new ProjectRoleTemplate();
        template.setProjectId(projectId);
        template.setRoleCode(roleCode);
        template.setTemplateName(roleCode + " template");
        template.setStatus(status);
        projectRoleTemplateMapper.insert(template);
        return template.getId();
    }

    private void insertRolePermission(Long templateId, String moduleKey, String accessLevel) {
        ProjectRoleTemplatePermission permission = new ProjectRoleTemplatePermission();
        permission.setProjectRoleTemplateId(templateId);
        permission.setModuleKey(moduleKey);
        permission.setAccessLevel(accessLevel);
        projectRoleTemplatePermissionMapper.insert(permission);
    }
}
