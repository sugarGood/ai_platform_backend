package com.aiplatform.backend.controller;

import com.aiplatform.backend.entity.AtomicCapability;
import com.aiplatform.backend.mapper.AtomicCapabilityMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/** 原子能力中心控制器（模块10）。 */
@RestController
@RequestMapping("/api/atomic-capabilities")
public class AtomicCapabilityController {

    private final AtomicCapabilityMapper mapper;

    public AtomicCapabilityController(AtomicCapabilityMapper mapper) {
        this.mapper = mapper;
    }

    @GetMapping
    public List<AtomicCapability> list(@RequestParam(required = false) String category) {
        var q = Wrappers.<AtomicCapability>lambdaQuery();
        if (category != null) q.eq(AtomicCapability::getCategory, category);
        return mapper.selectList(q.orderByDesc(AtomicCapability::getId));
    }

    @GetMapping("/{id}")
    public AtomicCapability getById(@PathVariable Long id) {
        AtomicCapability e = mapper.selectById(id);
        if (e == null) throw new RuntimeException("AtomicCapability not found: " + id);
        return e;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AtomicCapability create(@RequestBody AtomicCapability body) {
        if (body.getStatus() == null) body.setStatus("ACTIVE");
        if (body.getSubscriptionCount() == null) body.setSubscriptionCount(0);
        mapper.insert(body);
        return body;
    }

    @PutMapping("/{id}")
    public AtomicCapability update(@PathVariable Long id, @RequestBody AtomicCapability body) {
        AtomicCapability e = mapper.selectById(id);
        if (e == null) throw new RuntimeException("AtomicCapability not found: " + id);
        if (body.getName() != null) e.setName(body.getName());
        if (body.getDescription() != null) e.setDescription(body.getDescription());
        if (body.getDocContent() != null) e.setDocContent(body.getDocContent());
        if (body.getVersion() != null) e.setVersion(body.getVersion());
        mapper.updateById(e);
        return e;
    }

    @PostMapping("/{id}/deprecate")
    public AtomicCapability deprecate(@PathVariable Long id) {
        AtomicCapability e = mapper.selectById(id);
        if (e == null) throw new RuntimeException("AtomicCapability not found: " + id);
        e.setStatus("DEPRECATED");
        mapper.updateById(e);
        return e;
    }
}
