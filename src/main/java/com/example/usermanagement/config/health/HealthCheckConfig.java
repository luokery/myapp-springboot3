package com.example.usermanagement.config.health;

import org.springframework.context.annotation.Configuration;

/**
 * 健康检查配置
 * 
 * Spring Boot Actuator 会自动检测以下健康检查指示器:
 * - DataSourceHealthIndicator (db) - 检查数据库连接
 * - DiskSpaceHealthIndicator (diskSpace) - 检查磁盘空间
 * - 自定义的 CacheHealthIndicator (cache) - 检查缓存状态
 * - 自定义的 ApplicationHealthIndicator (application) - 检查应用状态
 * 
 * 配置详情见 application.yml 中的 management 配置
 */
@Configuration
public class HealthCheckConfig {
    // Spring Boot 自动注册所有 HealthIndicator Bean
    // 无需手动配置
}
