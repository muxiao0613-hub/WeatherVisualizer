package com.backend.lxy.controller;

import com.backend.lxy.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@Tag(name = "Health", description = "Health check APIs")
public class HealthController {

    @GetMapping("/health")
    @Operation(summary = "Health check")
    public ApiResponse<Map<String, String>> health() {
        Map<String, String> status = new HashMap<>();
        status.put("status", "ok");
        status.put("service", "Weather Visualizer API");
        status.put("version", "1.0.0");
        return ApiResponse.success(status);
    }
}
