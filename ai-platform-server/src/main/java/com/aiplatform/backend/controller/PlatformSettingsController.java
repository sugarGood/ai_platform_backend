package com.aiplatform.backend.controller;

import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 平台设置控制器（模块21）。
 */
@RestController
@RequestMapping("/api/admin/settings")
public class PlatformSettingsController {

    private final Map<String, String> settings = new ConcurrentHashMap<>(Map.of(
            "platform.name", "AI 中台",
            "platform.defaultModel", "claude-3-5-sonnet",
            "platform.maxTokensPerRequest", "100000",
            "platform.allowSelfRegistration", "false",
            "platform.defaultQuotaTokens", "200000"
    ));

    @GetMapping
    public Map<String, String> getAll() { return settings; }

    @GetMapping("/{key}")
    public Map<String, String> getOne(@PathVariable String key) {
        String value = settings.get(key);
        if (value == null) throw new RuntimeException("Setting not found: " + key);
        return Map.of("key", key, "value", value);
    }

    @PutMapping("/{key}")
    public Map<String, String> updateOne(@PathVariable String key,
                                         @RequestBody Map<String, String> body) {
        String value = body.get("value");
        if (value == null) throw new RuntimeException("Missing 'value' field");
        settings.put(key, value);
        return Map.of("key", key, "value", value);
    }

    @PutMapping
    public Map<String, String> updateBatch(@RequestBody Map<String, String> body) {
        settings.putAll(body);
        return settings;
    }
}
