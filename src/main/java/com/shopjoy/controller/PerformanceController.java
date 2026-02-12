package com.shopjoy.controller;

import com.shopjoy.aspect.PerformanceMetricsCollector;
import com.shopjoy.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Tag(name = "Performance", description = "APIs for system performance and optimization metrics")
@RestController
@RequestMapping("/api/v1/performance")
@RequiredArgsConstructor
public class PerformanceController {

    private final PerformanceMetricsCollector metricsCollector;

    @Operation(summary = "Get all performance metrics")
    @GetMapping("/metrics")
    public ResponseEntity<ApiResponse<Map<String, Map<String, Object>>>> getMetrics() {
        return ResponseEntity.ok(ApiResponse.success(metricsCollector.getAllMetrics(), "Metrics retrieved successfully"));
    }

    @Operation(summary = "Get cache statistics")
    @GetMapping("/cache")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCacheStats() {
        return ResponseEntity.ok(ApiResponse.success(metricsCollector.getCacheSummary(), "Cache stats retrieved successfully"));
    }
    
    @Operation(summary = "Get optimization health")
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getOptimizationHealth() {
        Map<String, Object> health = Map.of(
            "status", "UP",
            "optimizationLevel", "HIGH",
            "activeCollectors", List.of("PerformanceAspect", "CachingAspect", "LoggingAspect")
        );
        return ResponseEntity.ok(ApiResponse.success(health, "Performance health retrieved successfully"));
    }
}
