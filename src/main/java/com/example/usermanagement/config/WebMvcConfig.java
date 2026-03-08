package com.example.usermanagement.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    
    @Value("${file.upload-dir:/tmp/uploads/projects}")
    private String uploadDir;
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 配置静态资源映射，用于访问上传的项目图片
        registry.addResourceHandler("/uploads/projects/**")
                .addResourceLocations("file:" + uploadDir + "/");
    }
}
