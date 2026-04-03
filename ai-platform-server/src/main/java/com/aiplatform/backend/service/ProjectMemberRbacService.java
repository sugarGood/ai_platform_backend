package com.aiplatform.backend.service;

import com.aiplatform.backend.common.exception.BusinessException;
import com.aiplatform.backend.dto.ProjectMemberPermissionOverrideItemRequest;
import com.aiplatform.backend.dto.ProjectMemberPermissionOverridesRequest;
import com.aiplatform.backend.dto.ProjectMemberPermissionOverridesResponse;
import com.aiplatform.backend.dto.ProjectMemberResourceGrantsRequest;
import com.aiplatform.backend.entity.PlatformCredential;
import com.aiplatform.backend.entity.Project;
import com.aiplatform.backend.entity.ProjectAtomicCapability;
import com.aiplatform.backend.entity.ProjectKnowledgeConfig;
import com.aiplatform.backend.entity.ProjectMcpIntegration;
import com.aiplatform.backend.entity.ProjectMember;
import com.aiplatform.backend.entity.ProjectMemberPermissionOverride;
import com.aiplatform.backend.entity.ProjectMemberResourceGrant;
import com.aiplatform.backend.entity.ProjectSkill;
import com.aiplatform.backend.entity.ProjectTool;
import com.aiplatform.backend.mapper.ProjectAtomicCapabilityMapper;
import com.aiplatform.backend.mapper.ProjectKnowledgeConfigMapper;
import com.aiplatform.backend.mapper.ProjectMcpIntegrationMapper;
import com.aiplatform.backend.mapper.ProjectMemberPermissionOverrideMapper;
import com.aiplatform.backend.mapper.ProjectMemberResourceGrantMapper;
import com.aiplatform.backend.mapper.ProjectSkillMapper;
import com.aiplatform.backend.mapper.ProjectToolMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

/**
 * 项目成员 RBAC 服务，负责成员权限覆写、资源授权和能力收敛查询。
 */
@Service
public class ProjectMemberRbacService {

    private static final String INVALID_RESOURCE_GRANT_CODE = "INVALID_RESOURCE_GRANT";

    private final ProjectMemberService projectMemberService;
    private final PlatformCredentialService platformCredentialService;
    private final ProjectMemberPermissionOverrideMapper permissionOverrideMapper;
    private final ProjectMemberResourceGrantMapper resourceGrantMapper;
    private final ProjectKnowledgeConfigMapper projectKnowledgeConfigMapper;
    private final ProjectSkillMapper projectSkillMapper;
    private final ProjectToolMapper projectToolMapper;
    private final ProjectMcpIntegrationMapper projectMcpIntegrationMapper;
    private final ProjectAtomicCapabilityMapper projectAtomicCapabilityMapper;
    private final PermissionAssemblyService permissionAssemblyService;

    public ProjectMemberRbacService(ProjectMemberService projectMemberService,
                                    PlatformCredentialService platformCredentialService,
                                    ProjectMemberPermissionOverrideMapper permissionOverrideMapper,
                                    ProjectMemberResourceGrantMapper resourceGrantMapper,
                                    ProjectKnowledgeConfigMapper projectKnowledgeConfigMapper,
                                    ProjectSkillMapper projectSkillMapper,
                                    ProjectToolMapper projectToolMapper,
                                    ProjectMcpIntegrationMapper projectMcpIntegrationMapper,
                                    ProjectAtomicCapabilityMapper projectAtomicCapabilityMapper,
                                    PermissionAssemblyService permissionAssemblyService) {
        this.projectMemberService = projectMemberService;
        this.platformCredentialService = platformCredentialService;
        this.permissionOverrideMapper = permissionOverrideMapper;
        this.resourceGrantMapper = resourceGrantMapper;
        this.projectKnowledgeConfigMapper = projectKnowledgeConfigMapper;
        this.projectSkillMapper = projectSkillMapper;
        this.projectToolMapper = projectToolMapper;
        this.projectMcpIntegrationMapper = projectMcpIntegrationMapper;
        this.projectAtomicCapabilityMapper = projectAtomicCapabilityMapper;
        this.permissionAssemblyService = permissionAssemblyService;
    }

    /**
     * 保存成员模块权限覆写。
     */
    @Transactional(rollbackFor = Throwable.class)
    public ProjectMemberPermissionOverridesResponse savePermissionOverrides(Long projectId,
                                                                           Long memberId,
                                                                           ProjectMemberPermissionOverridesRequest request) {
        projectMemberService.getByProjectAndId(projectId, memberId);
        clearMemberOverrides(memberId);

        if (request.moduleOverrides() != null) {
            for (ProjectMemberPermissionOverrideItemRequest item : request.moduleOverrides()) {
                ProjectMemberPermissionOverride row = new ProjectMemberPermissionOverride();
                row.setProjectMemberId(memberId);
                row.setModuleKey(item.moduleKey());
                row.setAccessLevel(item.accessLevel());
                permissionOverrideMapper.insert(row);
            }
        }

        if (request.knowledgeBaseIds() != null || request.skillIds() != null || request.toolIds() != null
                || request.integrationIds() != null || request.atomicCapabilityIds() != null) {
            saveResourceGrants(projectId, memberId, new ProjectMemberResourceGrantsRequest(
                    request.knowledgeBaseIds(),
                    request.skillIds(),
                    request.toolIds(),
                    request.integrationIds(),
                    request.atomicCapabilityIds()));
        }

        return getPermissionOverrides(projectId, memberId);
    }

    /**
     * 保存成员资源授权范围。
     */
    @Transactional(rollbackFor = Throwable.class)
    public ProjectMemberPermissionOverridesResponse saveResourceGrants(Long projectId,
                                                                       Long memberId,
                                                                       ProjectMemberResourceGrantsRequest request) {
        projectMemberService.getByProjectAndId(projectId, memberId);
        clearMemberResourceGrants(memberId);

        insertResourceGrants(memberId, "KNOWLEDGE_BASE", request.knowledgeBaseIds(), loadProjectKnowledgeBaseIds(projectId));
        insertResourceGrants(memberId, "SKILL", request.skillIds(), loadProjectSkillIds(projectId));
        insertResourceGrants(memberId, "TOOL", request.toolIds(), loadProjectToolIds(projectId));
        insertResourceGrants(memberId, "INTEGRATION", request.integrationIds(), loadProjectIntegrationIds(projectId));
        insertResourceGrants(memberId, "ATOMIC_CAPABILITY", request.atomicCapabilityIds(), loadProjectAtomicCapabilityIds(projectId));

        return getPermissionOverrides(projectId, memberId);
    }

    /**
     * 查询成员权限收敛结果。
     */
    public ProjectMemberPermissionOverridesResponse getPermissionOverrides(Long projectId, Long memberId) {
        ProjectMember member = projectMemberService.getByProjectAndId(projectId, memberId);
        Project project = projectMemberService.getProjectById(projectId);
        PlatformCredential credential = platformCredentialService.getByUserId(member.getUserId());

        return permissionAssemblyService.assemble(
                project,
                member,
                credential,
                loadProjectKnowledgeBaseIds(projectId),
                loadProjectSkillIds(projectId),
                loadProjectToolIds(projectId),
                loadProjectIntegrationIds(projectId),
                loadProjectAtomicCapabilityIds(projectId)
        );
    }

    /**
     * 查询当前用户在指定项目下的能力收敛结果。
     */
    public ProjectMemberPermissionOverridesResponse getPermissionOverridesByUser(Long projectId, Long userId) {
        ProjectMember member = projectMemberService.getByProjectAndUserId(projectId, userId);
        return getPermissionOverrides(projectId, member.getId());
    }

    /**
     * 清空成员模块覆写。
     */
    @Transactional(rollbackFor = Throwable.class)
    public void clearPermissionOverrides(Long projectId, Long memberId) {
        projectMemberService.getByProjectAndId(projectId, memberId);
        clearMemberOverrides(memberId);
    }

    /**
     * 清空成员资源授权。
     */
    @Transactional(rollbackFor = Throwable.class)
    public void clearResourceGrants(Long projectId, Long memberId) {
        projectMemberService.getByProjectAndId(projectId, memberId);
        clearMemberResourceGrants(memberId);
    }

    private void clearMemberOverrides(Long memberId) {
        permissionOverrideMapper.delete(
                Wrappers.<ProjectMemberPermissionOverride>lambdaQuery()
                        .eq(ProjectMemberPermissionOverride::getProjectMemberId, memberId));
    }

    private void clearMemberResourceGrants(Long memberId) {
        resourceGrantMapper.delete(
                Wrappers.<ProjectMemberResourceGrant>lambdaQuery()
                        .eq(ProjectMemberResourceGrant::getProjectMemberId, memberId));
    }

    private void insertResourceGrants(Long memberId, String resourceType, List<Long> resourceIds, List<Long> allowedIds) {
        if (resourceIds == null || resourceIds.isEmpty()) {
            return;
        }
        Set<Long> allowed = Set.copyOf(allowedIds);
        for (Long resourceId : resourceIds) {
            if (!allowed.contains(resourceId)) {
                throw new BusinessException(400, INVALID_RESOURCE_GRANT_CODE, "资源授权超出项目可见范围");
            }
            ProjectMemberResourceGrant row = new ProjectMemberResourceGrant();
            row.setProjectMemberId(memberId);
            row.setResourceType(resourceType);
            row.setResourceId(resourceId);
            row.setGrantLevel("ALLOW");
            resourceGrantMapper.insert(row);
        }
    }

    private List<Long> loadProjectKnowledgeBaseIds(Long projectId) {
        return projectKnowledgeConfigMapper.selectList(Wrappers.<ProjectKnowledgeConfig>lambdaQuery()
                        .eq(ProjectKnowledgeConfig::getProjectId, projectId)
                        .eq(ProjectKnowledgeConfig::getStatus, "ACTIVE")
                        .orderByAsc(ProjectKnowledgeConfig::getId))
                .stream().map(ProjectKnowledgeConfig::getKbId).toList();
    }

    private List<Long> loadProjectSkillIds(Long projectId) {
        return projectSkillMapper.selectList(Wrappers.<ProjectSkill>lambdaQuery()
                        .eq(ProjectSkill::getProjectId, projectId)
                        .eq(ProjectSkill::getStatus, "ACTIVE")
                        .orderByAsc(ProjectSkill::getId))
                .stream().map(ProjectSkill::getSkillId).toList();
    }

    private List<Long> loadProjectToolIds(Long projectId) {
        return projectToolMapper.selectList(Wrappers.<ProjectTool>lambdaQuery()
                        .eq(ProjectTool::getProjectId, projectId)
                        .eq(ProjectTool::getStatus, "ACTIVE")
                        .orderByAsc(ProjectTool::getId))
                .stream().map(ProjectTool::getToolId).toList();
    }

    private List<Long> loadProjectIntegrationIds(Long projectId) {
        return projectMcpIntegrationMapper.selectList(Wrappers.<ProjectMcpIntegration>lambdaQuery()
                        .eq(ProjectMcpIntegration::getProjectId, projectId)
                        .eq(ProjectMcpIntegration::getStatus, "ACTIVE")
                        .orderByAsc(ProjectMcpIntegration::getId))
                .stream().map(ProjectMcpIntegration::getMcpServerId).toList();
    }

    private List<Long> loadProjectAtomicCapabilityIds(Long projectId) {
        return projectAtomicCapabilityMapper.selectList(Wrappers.<ProjectAtomicCapability>lambdaQuery()
                        .eq(ProjectAtomicCapability::getProjectId, projectId)
                        .eq(ProjectAtomicCapability::getStatus, "ACTIVE")
                        .orderByAsc(ProjectAtomicCapability::getId))
                .stream().map(ProjectAtomicCapability::getAtomicCapabilityId).toList();
    }
}
