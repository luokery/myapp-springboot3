package com.example.usermanagement.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置
 * 配置静态资源映射，用于访问上传的文件
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    
    @Value("${file.upload-dir:/tmp/uploads}")
    private String baseUploadDir;
    
    @Value("${file.public-dir:/tmp/uploads/public}")
    private String publicDir;
    
    @Value("${file.project-dir:/tmp/uploads/projects}")
    private String projectDir;
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 公开文件 - 不需要认证即可访问
        // 访问路径：/uploads/public/**
        registry.addResourceHandler("/uploads/public/**")
                .addResourceLocations("file:" + publicDir + "/");
        
        // 项目图片 - 兼容旧配置，公开访问
        // 访问路径：/uploads/projects/**
        registry.addResourceHandler("/uploads/projects/**")
                .addResourceLocations("file:" + projectDir + "/");
        
        // 注意：私密文件不配置静态资源映射
        // 私密文件需要通过 /api/files/download/{filename} 接口访问，需要认证
    }
}
