package com.aiplatform.backend.service;

import com.aiplatform.backend.dto.ProjectMemberAiCapabilitySummaryResponse;
import com.aiplatform.backend.dto.ProjectMemberModuleAccessResponse;
import com.aiplatform.backend.dto.ProjectMemberPermissionOverridesResponse;
import com.aiplatform.backend.dto.ProjectMemberScopedResourceSummaryResponse;
import com.aiplatform.backend.dto.ProjectMemberTokenQuotaSummaryResponse;
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
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** 权限装配服务：模板默认权限 -> 成员覆写 -> 资源授权收敛。 */
@Service
public class PermissionAssemblyService {

    private static final List<String> MODULE_KEYS = List.of("KNOWLEDGE_BASE", "SKILL", "TOOL", "INTEGRATION", "ATOMIC_CAPABILITY", "QUOTA");

    private final ProjectRoleTemplateMapper roleTemplateMapper;
    private final ProjectRoleTemplatePermissionMapper roleTemplatePermissionMapper;
    private final ProjectMemberPermissionOverrideMapper permissionOverrideMapper;
    private final ProjectMemberResourceGrantMapper resourceGrantMapper;

    public PermissionAssemblyService(ProjectRoleTemplateMapper roleTemplateMapper,
                                     ProjectRoleTemplatePermissionMapper roleTemplatePermissionMapper,
                                     ProjectMemberPermissionOverrideMapper permissionOverrideMapper,
                                     ProjectMemberResourceGrantMapper resourceGrantMapper) {
        this.roleTemplateMapper = roleTemplateMapper;
        this.roleTemplatePermissionMapper = roleTemplatePermissionMapper;
        this.permissionOverrideMapper = permissionOverrideMapper;
        this.resourceGrantMapper = resourceGrantMapper;
    }

    public ProjectMemberPermissionOverridesResponse assemble(Project project,
                                                             ProjectMember member,
                                                             PlatformCredential credential,
                                                             List<Long> knowledgeBaseIds,
                                                             List<Long> skillIds,
                                                             List<Long> toolIds,
                                                             List<Long> integrationIds,
                                                             List<Long> atomicCapabilityIds) {
        Map<String, String> defaults = loadRoleDefaults(member);
        Map<String, String> overrides = loadOverrides(member.getId());

        List<ProjectMemberModuleAccessResponse> roleDefaultModules = modules(defaults, Map.of());
        List<ProjectMemberModuleAccessResponse> effectiveModules = modules(defaults, overrides);

        ProjectMemberScopedResourceSummaryResponse kb = scoped(member.getId(), "KNOWLEDGE_BASE", access(defaults, overrides, "KNOWLEDGE_BASE"), ids(knowledgeBaseIds));
        ProjectMemberScopedResourceSummaryResponse sk = scoped(member.getId(), "SKILL", access(defaults, overrides, "SKILL"), ids(skillIds));
        ProjectMemberScopedResourceSummaryResponse tl = scoped(member.getId(), "TOOL", access(defaults, overrides, "TOOL"), ids(toolIds));
        ProjectMemberScopedResourceSummaryResponse ig = scoped(member.getId(), "INTEGRATION", access(defaults, overrides, "INTEGRATION"), ids(integrationIds));
        ProjectMemberScopedResourceSummaryResponse ac = scoped(member.getId(), "ATOMIC_CAPABILITY", access(defaults, overrides, "ATOMIC_CAPABILITY"), ids(atomicCapabilityIds));

        ProjectMemberTokenQuotaSummaryResponse quota = quota(project, credential);

        ProjectMemberAiCapabilitySummaryResponse roleDefaultSummary = new ProjectMemberAiCapabilitySummaryResponse(
                project.getId(), member.getId(), member.getUserId(), member.getRole(), false, roleDefaultModules,
                all(kb, ids(knowledgeBaseIds)), all(sk, ids(skillIds)), all(tl, ids(toolIds)),
                all(ig, ids(integrationIds)), all(ac, ids(atomicCapabilityIds)), quota);

        ProjectMemberAiCapabilitySummaryResponse effectiveSummary = new ProjectMemberAiCapabilitySummaryResponse(
                project.getId(), member.getId(), member.getUserId(), member.getRole(), !overrides.isEmpty(), effectiveModules,
                kb, sk, tl, ig, ac, quota);

        return new ProjectMemberPermissionOverridesResponse(
                project.getId(), member.getId(), member.getUserId(), member.getRole(),
                effectiveModules.stream().filter(ProjectMemberModuleAccessResponse::overridden).toList(),
                kb.selectedResourceIds(), sk.selectedResourceIds(), tl.selectedResourceIds(), ig.selectedResourceIds(), ac.selectedResourceIds(),
                quota, roleDefaultSummary, effectiveSummary);
    }

    private Map<String, String> loadRoleDefaults(ProjectMember member) {
        Map<String, String> result = defaults("FULL_CONTROL");
        ProjectRoleTemplate template = roleTemplateMapper.selectOne(Wrappers.<ProjectRoleTemplate>lambdaQuery()
                .eq(ProjectRoleTemplate::getProjectId, member.getProjectId())
                .eq(ProjectRoleTemplate::getRoleCode, member.getRole())
                .eq(ProjectRoleTemplate::getStatus, "ACTIVE")
                .last("LIMIT 1"));
        if (template == null) return result;

        List<ProjectRoleTemplatePermission> rows = roleTemplatePermissionMapper.selectList(Wrappers.<ProjectRoleTemplatePermission>lambdaQuery()
                .eq(ProjectRoleTemplatePermission::getProjectRoleTemplateId, template.getId()));
        if (rows.isEmpty()) return result;

        result = defaults("NONE");
        for (ProjectRoleTemplatePermission row : rows) result.put(row.getModuleKey(), row.getAccessLevel());
        return result;
    }

    private Map<String, String> loadOverrides(Long memberId) {
        List<ProjectMemberPermissionOverride> rows = permissionOverrideMapper.selectList(Wrappers.<ProjectMemberPermissionOverride>lambdaQuery()
                .eq(ProjectMemberPermissionOverride::getProjectMemberId, memberId));
        Map<String, String> result = new HashMap<>();
        for (ProjectMemberPermissionOverride row : rows) result.put(row.getModuleKey(), row.getAccessLevel());
        return result;
    }

    private List<ProjectMemberModuleAccessResponse> modules(Map<String, String> defaults, Map<String, String> overrides) {
        return MODULE_KEYS.stream().map(module -> new ProjectMemberModuleAccessResponse(
                module,
                overrides.containsKey(module) ? overrides.get(module) : defaults.getOrDefault(module, "NONE"),
                overrides.containsKey(module)
        )).toList();
    }

    private String access(Map<String, String> defaults, Map<String, String> overrides, String module) {
        return overrides.containsKey(module) ? overrides.get(module) : defaults.getOrDefault(module, "NONE");
    }

    private ProjectMemberScopedResourceSummaryResponse scoped(Long memberId, String resourceType, String accessLevel, List<Long> totalIds) {
        if ("NONE".equals(accessLevel)) return new ProjectMemberScopedResourceSummaryResponse(accessLevel, "NONE", totalIds.size(), 0, List.of(), List.of());

        List<ProjectMemberResourceGrant> grants = resourceGrantMapper.selectList(Wrappers.<ProjectMemberResourceGrant>lambdaQuery()
                .eq(ProjectMemberResourceGrant::getProjectMemberId, memberId)
                .eq(ProjectMemberResourceGrant::getResourceType, resourceType)
                .orderByAsc(ProjectMemberResourceGrant::getId));
        if (grants.isEmpty()) return new ProjectMemberScopedResourceSummaryResponse(accessLevel, "ALL", totalIds.size(), totalIds.size(), totalIds, List.of());

        Set<Long> selected = new LinkedHashSet<>();
        for (ProjectMemberResourceGrant grant : grants) selected.add(grant.getResourceId());
        List<Long> selectedIds = totalIds.stream().filter(selected::contains).toList();
        return new ProjectMemberScopedResourceSummaryResponse(accessLevel, "SELECTED", totalIds.size(), selectedIds.size(), selectedIds, List.of());
    }

    private ProjectMemberScopedResourceSummaryResponse all(ProjectMemberScopedResourceSummaryResponse source, List<Long> allIds) {
        return new ProjectMemberScopedResourceSummaryResponse(source.accessLevel(), "ALL", allIds.size(), allIds.size(), allIds, List.of());
    }

    private ProjectMemberTokenQuotaSummaryResponse quota(Project project, PlatformCredential credential) {
        Long personalQuota = credential != null ? credential.getMonthlyTokenQuota() : null;
        Long personalUsed = credential != null ? credential.getUsedTokensThisMonth() : null;
        Long personalRemaining = personalQuota != null && personalUsed != null ? Math.max(0L, personalQuota - personalUsed) : null;

        Long projectQuota = project.getMonthlyTokenQuota();
        Long projectUsed = project.getUsedTokensThisMonth() == null ? 0L : project.getUsedTokensThisMonth();
        Long projectRemaining = projectQuota == null ? null : Math.max(0L, projectQuota - projectUsed);

        return new ProjectMemberTokenQuotaSummaryResponse(
                credential != null ? credential.getId() : null,
                credential != null ? credential.getStatus() : "NONE",
                personalQuota, personalUsed, personalRemaining,
                credential != null ? credential.getAlertThresholdPct() : null,
                credential != null ? credential.getOverQuotaStrategy() : null,
                projectQuota, projectUsed, projectRemaining, project.getStatus());
    }

    private List<Long> ids(List<Long> source) { return source == null ? List.of() : source; }

    private Map<String, String> defaults(String accessLevel) {
        Map<String, String> map = new HashMap<>();
        for (String module : MODULE_KEYS) map.put(module, accessLevel);
        return map;
    }
}
