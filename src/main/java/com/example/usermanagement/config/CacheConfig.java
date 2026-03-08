package com.example.usermanagement.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.concurrent.TimeUnit;

/**
 * 缓存配置类
 * 使用 Caffeine 本地缓存
 * 当 Redis 可用时，可通过 RedissonConfig 配置分布式缓存
 */
@Configuration
@EnableCaching
public class CacheConfig {
    
    private static final Logger log = LoggerFactory.getLogger(CacheConfig.class);
    
    @Value("${cache.caffeine.default-expire:600}")
    private long defaultExpire;
    
    @Value("${cache.caffeine.max-size:1000}")
    private int maxSize;
    
    /**
     * Caffeine 本地缓存配置
     */
    @Bean
    public Caffeine<Object, Object> caffeineConfig() {
        return Caffeine.newBuilder()
                .initialCapacity(100)
                .maximumSize(maxSize)
                .expireAfterWrite(defaultExpire, TimeUnit.SECONDS)
                .recordStats();
    }
    
    /**
     * Caffeine 缓存管理器 (本地缓存)
     * 作为主要缓存管理器
     */
    @Bean("cacheManager")
    @Primary
    @ConditionalOnMissingBean
    public CacheManager cacheManager() {
        log.info("Initializing Caffeine cache manager with maxSize={}, defaultExpire={}s", maxSize, defaultExpire);
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(caffeineConfig());
        cacheManager.setAllowNullValues(false);
        return cacheManager;
    }
}
