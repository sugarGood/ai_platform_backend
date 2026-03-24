package com.aiplatform.backend.controller;

import com.aiplatform.backend.entity.AtomicCapability;
import com.aiplatform.backend.mapper.AtomicCapabilityMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/** 项目原子能力订阅控制器。 */
@RestController
@RequestMapping("/api/projects/{projectId}/atomic-capabilities")
public class ProjectAtomicCapabilityController {

    private final AtomicCapabilityMapper mapper;

    public ProjectAtomicCapabilityController(AtomicCapabilityMapper mapper) {
        this.mapper = mapper;
    }

    /** 查询项目已订阅的原子能力列表（TODO: 接入 project_atomic_capabilities 关联表）。 */
    @GetMapping
    public Map<String, Object> list(@PathVariable Long projectId) {
        return Map.of("projectId", projectId, "items", List.of(),
                "message", "project_atomic_capabilities 关联表待实现");
    }

    /** 项目订阅原子能力。 */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> subscribe(@PathVariable Long projectId,
                                         @RequestBody Map<String, Long> body) {
        Long capabilityId = body.get("capabilityId");
        AtomicCapability cap = mapper.selectById(capabilityId);
        if (cap == null) throw new RuntimeException("AtomicCapability not found: " + capabilityId);
        cap.setSubscriptionCount((cap.getSubscriptionCount() == null ? 0 : cap.getSubscriptionCount()) + 1);
        mapper.updateById(cap);
        return Map.of("projectId", projectId, "capabilityId", capabilityId, "status", "SUBSCRIBED");
    }

    /** 项目取消订阅原子能力。 */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unsubscribe(@PathVariable Long projectId, @PathVariable Long id) {
        // TODO: 从 project_atomic_capabilities 删除关联
    }
}
