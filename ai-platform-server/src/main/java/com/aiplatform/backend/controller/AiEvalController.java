package com.aiplatform.backend.controller;

import com.aiplatform.backend.mapper.AiUsageEventMapper;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

/**
 * AI 评估控制器（模块20）。
 * TODO: 接入 ai_eval_scores 表。
 */
@RestController
@RequestMapping("/api/admin/ai-eval-scores")
public class AiEvalController {

    private final AiUsageEventMapper aiUsageEventMapper;

    public AiEvalController(AiUsageEventMapper aiUsageEventMapper) {
        this.aiUsageEventMapper = aiUsageEventMapper;
    }

    @GetMapping
    public Map<String, Object> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Map.of(
                "items", List.of(),
                "total", 0,
                "page", page,
                "size", size,
                "message", "ai_eval_scores 表待实现"
        );
    }

    @GetMapping("/{id}")
    public Map<String, Object> getById(@PathVariable Long id) {
        return Map.of("id", id, "message", "AI评估详情待实现");
    }
}
