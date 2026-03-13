package com.example.usermanagement.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 应用管理控制器
 * 提供优雅停机等管理接口
 */
@Slf4j
@Tag(name = "应用管理", description = "应用状态管理和优雅停机接口")
@RestController
@RequestMapping("/api/admin")
public class ApplicationAdminController {

    @Autowired(required = false)
    private ConfigurableApplicationContext applicationContext;

    /**
     * 触发优雅停机
     * 注意：此接口仅用于测试和演示，生产环境应通过 SIGTERM 信号触发
     */
    @Operation(summary = "触发优雅停机", description = "手动触发应用的优雅停机（仅用于测试）")
    @PostMapping("/shutdown")
    public ResponseEntity<Result<Map<String, Object>>> shutdown() {
        log.info("收到优雅停机请求");
        
        Map<String, Object> data = new HashMap<>();
        data.put("message", "优雅停机已触发，应用将在处理完当前请求后关闭");
        data.put("timestamp", System.currentTimeMillis());
        
        // 异步执行关闭，以便响应能够返回
        new Thread(() -> {
            try {
                // 等待响应返回
                TimeUnit.MILLISECONDS.sleep(500);
                log.info("开始执行优雅停机...");
                
                if (applicationContext != null) {
                    // 触发 Spring 的优雅停机
                    applicationContext.close();
                } else {
                    // 如果没有 ApplicationContext，使用 System.exit
                    System.exit(0);
                }
            } catch (Exception e) {
                log.error("优雅停机异常", e);
            }
        }, "shutdown-hook").start();
        
        return ResponseEntity.ok(Result.success(data, "优雅停机请求已接收"));
    }

    /**
     * 获取应用状态
     */
    @Operation(summary = "获取应用状态", description = "获取应用当前的运行状态")
    @PostMapping("/status")
    public ResponseEntity<Result<Map<String, Object>>> getStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("status", "running");
        status.put("timestamp", System.currentTimeMillis());
        status.put("javaVersion", System.getProperty("java.version"));
        status.put("osName", System.getProperty("os.name"));
        status.put("availableProcessors", Runtime.getRuntime().availableProcessors());
        status.put("maxMemory", Runtime.getRuntime().maxMemory() / 1024 / 1024 + "MB");
        status.put("totalMemory", Runtime.getRuntime().totalMemory() / 1024 / 1024 + "MB");
        status.put("freeMemory", Runtime.getRuntime().freeMemory() / 1024 / 1024 + "MB");
        
        return ResponseEntity.ok(Result.success(status, "获取状态成功"));
    }
}
