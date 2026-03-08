package com.example.usermanagement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Schema(description = "用户响应信息")
public class UserResponseDTO {
    
    @Schema(description = "用户ID", example = "1")
    private Long id;
    
    @Schema(description = "用户名", example = "zhangsan")
    private String username;
    
    @Schema(description = "邮箱地址", example = "zhangsan@example.com")
    private String email;
    
    @Schema(description = "手机号码", example = "13800138000")
    private String phone;
    
    @Schema(description = "年龄", example = "25")
    private Integer age;
    
    @Schema(description = "角色", example = "user")
    private String role;
    
    @Schema(description = "状态：1-启用，0-禁用", example = "1")
    private Integer status;
    
    @Schema(description = "创建时间", example = "2024-01-01T10:00:00")
    private LocalDateTime createdAt;
    
    @Schema(description = "更新时间", example = "2024-01-01T10:00:00")
    private LocalDateTime updatedAt;
}
