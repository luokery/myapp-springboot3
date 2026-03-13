package com.example.usermanagement.config.health;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import java.util.Collection;

/**
 * 缓存健康检查
 * 检查 Caffeine 缓存是否正常工作
 */
@Slf4j
@Component
public class CacheHealthIndicator implements HealthIndicator {

    @Autowired(required = false)
    private CacheManager cacheManager;

    @Override
    public Health health() {
        try {
            if (cacheManager == null) {
                return Health.unknown()
                        .withDetail("message", "缓存管理器未初始化")
                        .build();
            }

            Collection<String> cacheNames = cacheManager.getCacheNames();
            
            // 检查缓存是否可用
            boolean cacheAvailable = true;
            int cacheCount = 0;
            
            for (String cacheName : cacheNames) {
                var cache = cacheManager.getCache(cacheName);
                if (cache == null) {
                    cacheAvailable = false;
                    log.warn("缓存 {} 不可用", cacheName);
                } else {
                    cacheCount++;
                }
            }

            if (cacheAvailable && cacheCount > 0) {
                return Health.up()
                        .withDetail("cacheCount", cacheCount)
                        .withDetail("cacheNames", cacheNames)
                        .build();
            } else if (cacheCount == 0) {
                return Health.unknown()
                        .withDetail("message", "没有配置缓存")
                        .build();
            } else {
                return Health.down()
                        .withDetail("message", "部分缓存不可用")
                        .withDetail("cacheNames", cacheNames)
                        .build();
            }
        } catch (Exception e) {
            log.error("缓存健康检查失败", e);
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
