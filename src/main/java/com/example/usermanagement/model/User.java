package com.example.usermanagement.model;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户实体
 * 包含乐观锁(version)和逻辑删除(deleted)字段
 */
@Data
public class User {
    private Long id;
    private String username;
    private String password;
    private String email;
    private String phone;
    private Integer age;
    private String role;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /**
     * 乐观锁版本号
     * 每次更新时自动+1，用于并发控制
     */
    private Integer version;
    
    /**
     * 逻辑删除标记
     * 0-未删除, 1-已删除
     */
    private Boolean deleted;
}
