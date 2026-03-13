package com.example.usermanagement.config;

import com.example.usermanagement.shiro.JwtFilter;
import com.example.usermanagement.shiro.JwtRealm;
import org.apache.shiro.mgt.DefaultSessionStorageEvaluator;
import org.apache.shiro.mgt.DefaultSubjectDAO;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.filter.authc.AnonymousFilter;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import jakarta.servlet.Filter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Shiro 配置类
 * 配置 JWT 无状态认证
 */
@Configuration
public class ShiroConfig {

    /**
     * 安全管理器
     */
    @Bean
    public DefaultWebSecurityManager securityManager(JwtRealm jwtRealm) {
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        securityManager.setRealm(jwtRealm);

        // 禁用 Session（无状态模式）
        DefaultSubjectDAO subjectDAO = new DefaultSubjectDAO();
        DefaultSessionStorageEvaluator sessionStorageEvaluator = new DefaultSessionStorageEvaluator();
        sessionStorageEvaluator.setSessionStorageEnabled(false);
        subjectDAO.setSessionStorageEvaluator(sessionStorageEvaluator);
        securityManager.setSubjectDAO(subjectDAO);

        return securityManager;
    }

    /**
     * Shiro 过滤器工厂
     */
    @Bean
    public ShiroFilterFactoryBean shiroFilterFactoryBean(DefaultWebSecurityManager securityManager, @Lazy JwtFilter jwtFilter) {
        ShiroFilterFactoryBean shiroFilter = new ShiroFilterFactoryBean();
        shiroFilter.setSecurityManager(securityManager);

        // 自定义过滤器
        Map<String, Filter> filters = new HashMap<>();
        filters.put("jwt", jwtFilter);
        filters.put("anon", new AnonymousFilter());
        shiroFilter.setFilters(filters);

        // 过滤链配置
        Map<String, String> filterChain = new LinkedHashMap<>();
        
        // 公开接口 - 不需要认证
        filterChain.put("/api/auth/login", "anon");
        filterChain.put("/api/auth/register", "anon");
        filterChain.put("/api/auth/refresh", "anon");
        
        // Swagger 和 API 文档 - 不需要认证
        filterChain.put("/swagger-ui/**", "anon");
        filterChain.put("/swagger-ui.html", "anon");
        filterChain.put("/v3/api-docs/**", "anon");
        filterChain.put("/webjars/**", "anon");
        
        // Druid 监控 - 不需要认证（生产环境建议加上认证）
        filterChain.put("/druid/**", "anon");
        
        // H2 控制台 - 不需要认证
        filterChain.put("/h2/**", "anon");
        
        // Spring Boot Actuator 端点 - 不需要认证（生产环境建议加上认证）
        filterChain.put("/actuator/**", "anon");
        filterChain.put("/actuator", "anon");
        
        // 文件上传相关 - 公开文件上传接口不需要认证
        filterChain.put("/api/files/upload/image/public", "anon");
        
        // 公开文件访问 - 不需要认证
        filterChain.put("/uploads/public/**", "anon");
        filterChain.put("/uploads/projects/**", "anon");
        
        // 静态资源 - 不需要认证
        filterChain.put("/static/**", "anon");
        filterChain.put("/*.html", "anon");
        filterChain.put("/*.css", "anon");
        filterChain.put("/*.js", "anon");
        filterChain.put("/favicon.ico", "anon");
        filterChain.put("/", "anon");
        
        // 其他所有请求都需要 JWT 认证
        filterChain.put("/**", "jwt");

        shiroFilter.setFilterChainDefinitionMap(filterChain);
        
        // 未授权跳转（API 返回 JSON，不跳转页面）
        shiroFilter.setUnauthorizedUrl("/api/auth/unauthorized");

        return shiroFilter;
    }
}
