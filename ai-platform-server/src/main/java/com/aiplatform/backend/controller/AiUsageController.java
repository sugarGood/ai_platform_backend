package com.aiplatform.backend.controller;

import com.aiplatform.backend.common.dto.PageResponse;
import com.aiplatform.backend.dto.AiUsageEventResponse;
import com.aiplatform.backend.service.AiUsageService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * AI 用量查询控制器。
 *
 * <p>提供 AI 用量明细的分页查询 REST API，路径前缀为 {@code /api/usage-events}。</p>
 */
@RestController
@RequestMapping("/api/usage-events")
public class AiUsageController {

    private final AiUsageService aiUsageService;

    /**
     * 构造函数，注入 AI 用量业务服务。
     *
     * @param aiUsageService AI 用量业务服务
     */
    public AiUsageController(AiUsageService aiUsageService) {
        this.aiUsageService = aiUsageService;
    }

    /**
     * 分页查询 AI 用量明细。
     *
     * <p>支持按用户ID和项目ID过滤，默认第1页，每页20条。</p>
     *
     * @param userId    可选的用户ID过滤条件
     * @param projectId 可选的项目ID过滤条件
     * @param page      页码，默认为1
     * @param size      每页大小，默认为20
     * @return 分页后的 AI 用量事件响应
     */
    @GetMapping
    public PageResponse<AiUsageEventResponse> list(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long projectId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return aiUsageService.listUsageEvents(userId, projectId, page, size);
    }
}
