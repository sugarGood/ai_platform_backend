package com.aiplatform.backend.dto;

import java.util.List;

public record ProjectMemberResourceGrantsRequest(
        List<Long> knowledgeBaseIds,
        List<Long> skillIds,
        List<Long> toolIds,
        List<Long> integrationIds,
        List<Long> atomicCapabilityIds
) {
}
