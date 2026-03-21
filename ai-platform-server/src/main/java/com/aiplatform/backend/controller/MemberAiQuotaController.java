package com.aiplatform.backend.controller;

import com.aiplatform.backend.dto.CreateMemberAiQuotaRequest;
import com.aiplatform.backend.dto.MemberAiQuotaResponse;
import com.aiplatform.backend.service.AiUsageService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 成员 AI 配额管理控制器。
 *
 * <p>提供成员配额的创建和查询等 REST API，路径前缀为 {@code /api/member-quotas}。</p>
 */
@RestController
@RequestMapping("/api/member-quotas")
public class MemberAiQuotaController {

    private final AiUsageService aiUsageService;

    /**
     * 构造函数，注入 AI 用量业务服务。
     *
     * @param aiUsageService AI 用量业务服务
     */
    public MemberAiQuotaController(AiUsageService aiUsageService) {
        this.aiUsageService = aiUsageService;
    }

    /**
     * 创建成员 AI 配额。
     *
     * @param request 创建配额请求体
     * @return 新创建的配额响应
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MemberAiQuotaResponse create(@Valid @RequestBody CreateMemberAiQuotaRequest request) {
        return MemberAiQuotaResponse.from(aiUsageService.createQuota(request));
    }

    /**
     * 查询成员配额列表，按用户ID或项目ID过滤。
     *
     * <p>至少需要提供 userId 或 projectId 中的一个，否则返回空列表。</p>
     *
     * @param userId    可选的用户ID过滤条件
     * @param projectId 可选的项目ID过滤条件
     * @return 配额响应列表
     */
    @GetMapping
    public List<MemberAiQuotaResponse> list(@RequestParam(required = false) Long userId,
                                            @RequestParam(required = false) Long projectId) {
        if (userId != null) {
            return aiUsageService.listQuotasByUserId(userId).stream().map(MemberAiQuotaResponse::from).toList();
        }
        if (projectId != null) {
            return aiUsageService.listQuotasByProjectId(projectId).stream().map(MemberAiQuotaResponse::from).toList();
        }
        return List.of();
    }
}
