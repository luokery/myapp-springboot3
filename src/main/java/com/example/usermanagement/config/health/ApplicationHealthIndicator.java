package com.example.usermanagement.config.health;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;

/**
 * 应用健康检查
 * 检查应用运行状态、内存使用率等
 */
@Slf4j
@Component
public class ApplicationHealthIndicator implements HealthIndicator {

    @Value("${spring.application.name:user-management}")
    private String applicationName;

    @Override
    public Health health() {
        try {
            // 获取运行时信息
            RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
            MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
            OperatingSystemMXBean osMXBean = ManagementFactory.getOperatingSystemMXBean();

            // JVM 内存使用情况
            long heapUsed = memoryMXBean.getHeapMemoryUsage().getUsed();
            long heapMax = memoryMXBean.getHeapMemoryUsage().getMax();
            double heapUsagePercent = (double) heapUsed / heapMax * 100;

            // 系统负载
            double systemLoadAverage = osMXBean.getSystemLoadAverage();
            int availableProcessors = osMXBean.getAvailableProcessors();

            // JVM 运行时间
            long uptimeMillis = runtimeMXBean.getUptime();
            long uptimeSeconds = uptimeMillis / 1000;
            long uptimeMinutes = uptimeSeconds / 60;
            long uptimeHours = uptimeMinutes / 60;

            // 构建健康状态
            Health.Builder builder;
            
            // 如果堆内存使用率超过 90%，标记为 DOWN
            if (heapUsagePercent > 90) {
                builder = Health.down();
            }
            // 如果堆内存使用率超过 75%，标记为警告
            else if (heapUsagePercent > 75) {
                builder = Health.status("WARNING");
            } else {
                builder = Health.up();
            }

            return builder
                    .withDetail("application", applicationName)
                    .withDetail("uptime", String.format("%d小时 %d分钟 %d秒", 
                            uptimeHours, uptimeMinutes % 60, uptimeSeconds % 60))
                    .withDetail("uptimeMillis", uptimeMillis)
                    .withDetail("heapMemory", String.format("%.2f MB / %.2f MB (%.1f%%)",
                            heapUsed / 1024.0 / 1024.0,
                            heapMax / 1024.0 / 1024.0,
                            heapUsagePercent))
                    .withDetail("heapUsedBytes", heapUsed)
                    .withDetail("heapMaxBytes", heapMax)
                    .withDetail("heapUsagePercent", String.format("%.1f%%", heapUsagePercent))
                    .withDetail("systemLoadAverage", systemLoadAverage >= 0 ? 
                            String.format("%.2f", systemLoadAverage) : "N/A")
                    .withDetail("availableProcessors", availableProcessors)
                    .withDetail("javaVersion", System.getProperty("java.version"))
                    .withDetail("javaVendor", System.getProperty("java.vendor"))
                    .build();
        } catch (Exception e) {
            log.error("应用健康检查失败", e);
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
