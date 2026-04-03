package com.aiplatform.backend.controller;

import com.aiplatform.backend.dto.PlatformSettingResponse;
import com.aiplatform.backend.dto.PlatformSettingValueRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
    public Map<String, String> getAll() {
        return settings;
    }

    @GetMapping("/{key}")
    public PlatformSettingResponse getOne(@PathVariable String key) {
        String value = settings.get(key);
        if (value == null) {
            throw new RuntimeException("Setting not found: " + key);
        }
        return new PlatformSettingResponse(key, value);
    }

    @PutMapping("/{key}")
    public PlatformSettingResponse updateOne(@PathVariable String key,
                                             @Valid @RequestBody PlatformSettingValueRequest request) {
        settings.put(key, request.value());
        return new PlatformSettingResponse(key, request.value());
    }

    @PutMapping
    public Map<String, String> updateBatch(@RequestBody Map<String, String> body) {
        settings.putAll(body);
        return settings;
    }
}
