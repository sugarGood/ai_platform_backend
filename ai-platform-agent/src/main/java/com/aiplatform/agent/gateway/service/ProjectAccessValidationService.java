package com.aiplatform.agent.gateway.service;

import com.aiplatform.agent.gateway.entity.PlatformCredentialRef;
import com.aiplatform.agent.gateway.entity.ProjectMemberRef;
import com.aiplatform.agent.gateway.mapper.ProjectMemberRefMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.stereotype.Service;

/**
 * 项目访问权限校验。
 */
@Service
public class ProjectAccessValidationService {

    private final ProjectMemberRefMapper projectMemberRefMapper;

    public ProjectAccessValidationService(ProjectMemberRefMapper projectMemberRefMapper) {
        this.projectMemberRefMapper = projectMemberRefMapper;
    }

    /**
     * 校验凭证是否可访问指定项目。
     */
    public void validate(PlatformCredentialRef credential, Long projectId) {
        if (projectId == null) {
            return;
        }

        Long boundProjectId = credential.getBoundProjectId();
        if (boundProjectId != null && !boundProjectId.equals(projectId)) {
            throw new ProjectAccessDeniedException("凭证未绑定当前项目");
        }

        long memberCount = projectMemberRefMapper.selectCount(
                Wrappers.<ProjectMemberRef>lambdaQuery()
                        .eq(ProjectMemberRef::getProjectId, projectId)
                        .eq(ProjectMemberRef::getUserId, credential.getUserId())
        );
        if (memberCount <= 0) {
            throw new ProjectAccessDeniedException("无项目访问权限");
        }
    }

    public static class ProjectAccessDeniedException extends RuntimeException {
        public ProjectAccessDeniedException(String message) {
            super(message);
        }
    }
}
