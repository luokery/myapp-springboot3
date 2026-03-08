package com.example.usermanagement.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.util.Collection;

/**
 * 缓存管理服务
 * 提供手动清除缓存的功能
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CacheService {

    private final CacheManager cacheManager;

    /**
     * 清除所有缓存
     */
    public void clearAllCaches() {
        Collection<String> cacheNames = cacheManager.getCacheNames();
        for (String cacheName : cacheNames) {
            clearCache(cacheName);
        }
        log.info("已清除所有缓存: {}", cacheNames);
    }

    /**
     * 清除指定名称的缓存
     */
    public void clearCache(String cacheName) {
        var cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.clear();
            log.info("已清除缓存: {}", cacheName);
        }
    }

    /**
     * 清除指定缓存中的指定key
     */
    public void evict(String cacheName, Object key) {
        var cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.evict(key);
            log.info("已清除缓存 {} 的 key: {}", cacheName, key);
        }
    }

    /**
     * 获取缓存统计信息
     */
    public String getCacheStats() {
        StringBuilder sb = new StringBuilder("缓存状态:\n");
        Collection<String> cacheNames = cacheManager.getCacheNames();
        for (String cacheName : cacheNames) {
            var cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                sb.append(String.format("  - %s: %s\n", cacheName, cache.getNativeCache().getClass().getSimpleName()));
            }
        }
        return sb.toString();
    }
}
