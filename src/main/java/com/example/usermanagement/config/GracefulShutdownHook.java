package com.example.usermanagement.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 优雅停机钩子
 * 在应用关闭时执行资源清理工作
 */
@Slf4j
@Component
public class GracefulShutdownHook implements ApplicationListener<ContextClosedEvent> {

    private final AtomicInteger shutdownCounter = new AtomicInteger(0);

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        int count = shutdownCounter.incrementAndGet();
        if (count > 1) {
            log.warn("优雅停机已触发，跳过重复执行");
            return;
        }

        log.info("========================================");
        log.info("开始执行优雅停机...");
        log.info("========================================");

        ApplicationContext context = event.getApplicationContext();
        
        // 记录停机开始时间
        long startTime = System.currentTimeMillis();

        try {
            // 1. 停止接收新请求（由 Spring Boot 的 graceful shutdown 处理）
            log.info("[1/4] 等待正在处理的请求完成...");

            // 2. 清理缓存
            cleanupCaches(context);
            log.info("[2/4] 缓存清理完成");

            // 3. 关闭线程池
            shutdownExecutorServices(context);
            log.info("[3/4] 线程池关闭完成");

            // 4. 关闭数据库连接池
            cleanupDataSource(context);
            log.info("[4/4] 数据库连接池关闭完成");

            long duration = System.currentTimeMillis() - startTime;
            log.info("========================================");
            log.info("优雅停机完成，耗时: {}ms", duration);
            log.info("========================================");

        } catch (Exception e) {
            log.error("优雅停机过程中发生异常", e);
        }
    }

    /**
     * 清理缓存
     */
    private void cleanupCaches(ApplicationContext context) {
        try {
            // 获取 CacheManager 并清理缓存
            org.springframework.cache.CacheManager cacheManager = 
                context.getBeanProvider(org.springframework.cache.CacheManager.class).getIfAvailable();
            
            if (cacheManager != null) {
                log.info("正在清理缓存: {}", cacheManager.getCacheNames());
                cacheManager.getCacheNames().forEach(cacheName -> {
                    org.springframework.cache.Cache cache = cacheManager.getCache(cacheName);
                    if (cache != null) {
                        cache.clear();
                        log.debug("已清理缓存: {}", cacheName);
                    }
                });
            }
        } catch (Exception e) {
            log.warn("清理缓存时发生异常: {}", e.getMessage());
        }
    }

    /**
     * 关闭线程池
     */
    private void shutdownExecutorServices(ApplicationContext context) {
        try {
            // 获取所有 ExecutorService Bean
            String[] beanNames = context.getBeanNamesForType(ExecutorService.class);
            for (String beanName : beanNames) {
                ExecutorService executor = context.getBean(beanName, ExecutorService.class);
                if (!executor.isShutdown()) {
                    log.info("正在关闭线程池: {}", beanName);
                    executor.shutdown();
                    // 等待任务完成，最多等待 10 秒
                    if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                        log.warn("线程池 {} 未能在指定时间内关闭，强制关闭", beanName);
                        executor.shutdownNow();
                    }
                }
            }
        } catch (Exception e) {
            log.warn("关闭线程池时发生异常: {}", e.getMessage());
        }
    }

    /**
     * 清理数据源
     */
    private void cleanupDataSource(ApplicationContext context) {
        try {
            // 获取 Druid 数据源
            com.alibaba.druid.pool.DruidDataSource dataSource = 
                context.getBeanProvider(com.alibaba.druid.pool.DruidDataSource.class).getIfAvailable();
            
            if (dataSource != null && !dataSource.isClosed()) {
                log.info("正在关闭 Druid 数据源连接池...");
                log.info("活跃连接数: {}, 空闲连接数: {}", 
                    dataSource.getActiveCount(), dataSource.getPoolingCount());
                dataSource.close();
                log.info("Druid 数据源连接池已关闭");
            }
        } catch (Exception e) {
            log.warn("关闭数据源时发生异常: {}", e.getMessage());
        }
    }
}
