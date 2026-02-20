package com.shopjoy.controller;

import com.shopjoy.aspect.PerformanceMetricsCollector;
import com.shopjoy.dto.response.ApiResponse;
import com.shopjoy.util.CacheMetricsCollector;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    private final CacheMetricsCollector cacheMetricsCollector;

    @Operation(summary = "Get all performance metrics")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/metrics")
    public ResponseEntity<ApiResponse<Map<String, Map<String, Object>>>> getMetrics() {
        return ResponseEntity.ok(ApiResponse.success(metricsCollector.getAllMetrics(), "Metrics retrieved successfully"));
    }

    @Operation(summary = "Get cache statistics")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/cache")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCacheStats() {
        return ResponseEntity.ok(ApiResponse.success(cacheMetricsCollector.getCacheSummary(), "Cache stats retrieved successfully"));
    }

    @Operation(summary = "Get detailed cache statistics")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/cache/details")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCacheDetails() {
        return ResponseEntity.ok(ApiResponse.success(cacheMetricsCollector.getAllCacheStats(), "Detailed cache stats retrieved successfully"));
    }
    
    @Operation(summary = "Get optimization health")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getOptimizationHealth() {
        Map<String, Object> health = Map.of(
            "status", "UP",
            "optimizationLevel", "HIGH",
            "activeCollectors", List.of("PerformanceAspect", "LoggingAspect", "CaffeineCacheStats")
        );
        return ResponseEntity.ok(ApiResponse.success(health, "Performance health retrieved successfully"));
    }
}
