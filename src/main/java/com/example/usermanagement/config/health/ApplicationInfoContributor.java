package com.example.usermanagement.config.health;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 应用信息贡献者
 * 为 /actuator/info 端点提供自定义信息
 */
@Component
public class ApplicationInfoContributor implements InfoContributor {

    @Value("${spring.application.name:user-management}")
    private String applicationName;

    @Value("${server.port:8080}")
    private String serverPort;

    @Override
    public void contribute(Info.Builder builder) {
        Map<String, Object> appDetails = new HashMap<>();
        
        appDetails.put("name", applicationName);
        appDetails.put("description", "用户管理与项目管理系统");
        appDetails.put("version", "1.0.0");
        appDetails.put("serverPort", serverPort);
        appDetails.put("startTime", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        // 功能特性
        Map<String, Boolean> features = new HashMap<>();
        features.put("userManagement", true);
        features.put("projectManagement", true);
        features.put("imageUpload", true);
        features.put("jwtAuth", true);
        features.put("cache", true);
        features.put("actuator", true);
        appDetails.put("features", features);
        
        // 技术栈
        Map<String, String> techStack = new HashMap<>();
        techStack.put("framework", "Spring Boot 3.2.0");
        techStack.put("java", "Java 17");
        techStack.put("database", "H2 (In-Memory)");
        techStack.put("orm", "MyBatis 3.0.3");
        techStack.put("cache", "Caffeine");
        techStack.put("security", "Shiro + JWT");
        techStack.put("monitoring", "Spring Boot Actuator");
        appDetails.put("techStack", techStack);
        
        builder.withDetail("application", appDetails);
    }
}
