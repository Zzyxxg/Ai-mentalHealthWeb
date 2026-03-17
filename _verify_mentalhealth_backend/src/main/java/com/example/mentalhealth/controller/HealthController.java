package com.example.mentalhealth.controller;

import com.example.mentalhealth.common.Result;
import com.example.mentalhealth.config.TraceIdFilter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "System", description = "系统监控")
@RestController
@RequestMapping("/api/v1")
public class HealthController {

    @Operation(summary = "健康检查")
    @GetMapping("/health")
    public ResponseEntity<Result<Map<String, Object>>> health() {
        Map<String, Object> data = new HashMap<>();
        data.put("status", "UP");
        data.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(Result.success(data, TraceIdFilter.currentTraceId()));
    }
}
