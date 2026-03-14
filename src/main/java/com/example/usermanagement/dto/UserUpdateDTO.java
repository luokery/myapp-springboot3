package com.example.usermanagement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "用户更新请求")
public class UserUpdateDTO {
    
    @Schema(description = "用户名", example = "zhangsan", minLength = 2, maxLength = 50)
    @Size(min = 2, max = 50, message = "用户名长度必须在2-50个字符之间")
    private String username;
    
    @Schema(description = "邮箱地址", example = "zhangsan@example.com")
    @Email(message = "邮箱格式不正确")
    private String email;
    
    @Schema(description = "手机号码", example = "13800138000")
    private String phone;
    
    @Schema(description = "年龄", example = "25", minimum = "1", maximum = "150")
    private Integer age;
    
    @Schema(description = "角色", example = "user", allowableValues = {"user", "admin"})
    private String role;
    
    @Schema(description = "状态：1-启用，0-禁用", example = "1", allowableValues = {"0", "1"})
    private Integer status;
    
    @Schema(description = "乐观锁版本号（必须与当前数据库中的版本一致）", example = "1", required = true)
    private Integer version;
}
