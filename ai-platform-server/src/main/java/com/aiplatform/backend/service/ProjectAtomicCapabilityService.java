package com.aiplatform.backend.service;

import com.aiplatform.backend.common.exception.BusinessException;
import com.aiplatform.backend.dto.ProjectAtomicSubscriptionResponse;
import com.aiplatform.backend.entity.AtomicCapability;
import com.aiplatform.backend.entity.ProjectAtomicCapability;
import com.aiplatform.backend.mapper.AtomicCapabilityMapper;
import com.aiplatform.backend.mapper.ProjectAtomicCapabilityMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 项目原子能力订阅：读写 {@code project_atomic_capabilities}，并维护 {@code atomic_capabilities.subscription_count}。
 */
@Service
public class ProjectAtomicCapabilityService {

    private final ProjectAtomicCapabilityMapper projectAtomicCapabilityMapper;
    private final AtomicCapabilityMapper atomicCapabilityMapper;
    private final ProjectService projectService;

    public ProjectAtomicCapabilityService(ProjectAtomicCapabilityMapper projectAtomicCapabilityMapper,
                                          AtomicCapabilityMapper atomicCapabilityMapper,
                                          ProjectService projectService) {
        this.projectAtomicCapabilityMapper = projectAtomicCapabilityMapper;
        this.atomicCapabilityMapper = atomicCapabilityMapper;
        this.projectService = projectService;
    }

    public List<ProjectAtomicSubscriptionResponse> listByProjectId(Long projectId) {
        projectService.getByIdOrThrow(projectId);
        List<ProjectAtomicCapability> rows = projectAtomicCapabilityMapper.selectList(
                Wrappers.<ProjectAtomicCapability>lambdaQuery()
                        .eq(ProjectAtomicCapability::getProjectId, projectId)
                        .orderByAsc(ProjectAtomicCapability::getId));
        return rows.stream().map(row -> {
            AtomicCapability cap = atomicCapabilityMapper.selectById(row.getAtomicCapabilityId());
            return ProjectAtomicSubscriptionResponse.from(row, cap);
        }).toList();
    }

    @Transactional(rollbackFor = Throwable.class)
    public ProjectAtomicSubscriptionResponse subscribe(Long projectId, Long capabilityId) {
        projectService.getByIdOrThrow(projectId);
        AtomicCapability cap = atomicCapabilityMapper.selectById(capabilityId);
        if (cap == null) {
            throw new BusinessException(404, "ATOMIC_CAPABILITY_NOT_FOUND", "原子能力不存在");
        }

        ProjectAtomicCapability existing = projectAtomicCapabilityMapper.selectOne(
                Wrappers.<ProjectAtomicCapability>lambdaQuery()
                        .eq(ProjectAtomicCapability::getProjectId, projectId)
                        .eq(ProjectAtomicCapability::getAtomicCapabilityId, capabilityId)
                        .last("LIMIT 1"));

        if (existing != null) {
            if ("DISABLED".equals(existing.getStatus())) {
                existing.setStatus("ACTIVE");
                projectAtomicCapabilityMapper.updateById(existing);
                adjustSubscriptionCount(capabilityId, 1);
            }
            return ProjectAtomicSubscriptionResponse.from(existing, cap);
        }

        ProjectAtomicCapability row = ProjectAtomicCapability.builder()
                .projectId(projectId)
                .atomicCapabilityId(capabilityId)
                .status("ACTIVE")
                .build();
        projectAtomicCapabilityMapper.insert(row);
        adjustSubscriptionCount(capabilityId, 1);
        return ProjectAtomicSubscriptionResponse.from(row, cap);
    }

    @Transactional(rollbackFor = Throwable.class)
    public void unsubscribe(Long projectId, Long subscriptionId) {
        projectService.getByIdOrThrow(projectId);
        ProjectAtomicCapability row = projectAtomicCapabilityMapper.selectById(subscriptionId);
        if (row == null || !projectId.equals(row.getProjectId())) {
            throw new BusinessException(404, "PROJECT_ATOMIC_SUBSCRIPTION_NOT_FOUND", "项目原子能力订阅不存在");
        }
        if ("ACTIVE".equals(row.getStatus())) {
            adjustSubscriptionCount(row.getAtomicCapabilityId(), -1);
        }
        projectAtomicCapabilityMapper.deleteById(subscriptionId);
    }

    private void adjustSubscriptionCount(Long capabilityId, int delta) {
        AtomicCapability cap = atomicCapabilityMapper.selectById(capabilityId);
        if (cap == null) {
            return;
        }
        int base = cap.getSubscriptionCount() != null ? cap.getSubscriptionCount() : 0;
        int next = Math.max(0, base + delta);
        cap.setSubscriptionCount(next);
        atomicCapabilityMapper.updateById(cap);
    }
}
