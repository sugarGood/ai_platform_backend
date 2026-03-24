package com.aiplatform.backend.controller;

import com.aiplatform.backend.entity.NotificationChannel;
import com.aiplatform.backend.mapper.NotificationChannelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/** 通知渠道管理控制器（模块14）。 */
@RestController
@RequestMapping("/api/admin/notification-channels")
public class NotificationChannelController {

    private final NotificationChannelMapper mapper;

    public NotificationChannelController(NotificationChannelMapper mapper) {
        this.mapper = mapper;
    }

    @GetMapping
    public List<NotificationChannel> list() {
        return mapper.selectList(null);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public NotificationChannel create(@RequestBody NotificationChannel body) {
        if (body.getStatus() == null) body.setStatus("ACTIVE");
        if (body.getIsDefault() == null) body.setIsDefault(false);
        mapper.insert(body);
        return body;
    }

    @PutMapping("/{id}")
    public NotificationChannel update(@PathVariable Long id,
                                      @RequestBody NotificationChannel body) {
        NotificationChannel e = mapper.selectById(id);
        if (e == null) throw new RuntimeException("NotificationChannel not found: " + id);
        if (body.getName() != null) e.setName(body.getName());
        if (body.getConfig() != null) e.setConfig(body.getConfig());
        mapper.updateById(e);
        return e;
    }

    /** 测试通知渠道（发送测试消息）。TODO: 接入实际通知服务。 */
    @PostMapping("/{id}/test")
    public Map<String, Object> test(@PathVariable Long id) {
        NotificationChannel e = mapper.selectById(id);
        if (e == null) throw new RuntimeException("NotificationChannel not found: " + id);
        return Map.of("channelId", id, "channelType", e.getChannelType(),
                "status", "pending", "message", "通知服务待集成");
    }
}
