package com.example.usermanagement.controller;

import com.example.usermanagement.service.CacheService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 缓存管理接口
 */
@Tag(name = "缓存管理", description = "缓存管理相关接口")
@RestController
@RequestMapping("/api/cache")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CacheController {

    private final CacheService cacheService;

    @Operation(summary = "清除所有缓存", description = "清除系统中所有缓存数据")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "缓存清除成功")
    })
    @DeleteMapping("/all")
    public ResponseEntity<Result<String>> clearAllCaches() {
        cacheService.clearAllCaches();
        return ResponseEntity.ok(Result.success("所有缓存已清除", "操作成功"));
    }

    @Operation(summary = "清除指定缓存", description = "清除指定名称的缓存")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "缓存清除成功")
    })
    @DeleteMapping("/{cacheName}")
    public ResponseEntity<Result<String>> clearCache(
            @Parameter(description = "缓存名称 (users/projects/statistics)", required = true)
            @PathVariable String cacheName) {
        cacheService.clearCache(cacheName);
        return ResponseEntity.ok(Result.success("缓存 " + cacheName + " 已清除", "操作成功"));
    }

    @Operation(summary = "获取缓存状态", description = "获取系统中各缓存的状态信息")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取成功")
    })
    @GetMapping("/stats")
    public ResponseEntity<Result<Map<String, Object>>> getCacheStats() {
        Map<String, Object> data = new HashMap<>();
        data.put("stats", cacheService.getCacheStats());
        return ResponseEntity.ok(Result.success(data, "获取缓存状态成功"));
    }
}
