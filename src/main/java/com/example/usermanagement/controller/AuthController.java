package com.example.usermanagement.controller;

import com.example.usermanagement.dto.LoginDTO;
import com.example.usermanagement.dto.RegisterDTO;
import com.example.usermanagement.dto.UserResponseDTO;
import com.example.usermanagement.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证控制器
 * 处理登录、注册、刷新 Token 等请求
 */
@Tag(name = "认证管理", description = "用户登录、注册、Token 刷新等接口")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 用户登录
     */
    @Operation(summary = "用户登录", description = "使用用户名和密码登录，返回 JWT Token")
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginDTO loginDTO) {
        String token = authService.login(loginDTO);
        UserResponseDTO user = authService.getCurrentUser(loginDTO.getUsername());
        
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("user", user);
        
        return Result.success(data, "登录成功");
    }

    /**
     * 用户注册
     */
    @Operation(summary = "用户注册", description = "注册新用户账号")
    @PostMapping("/register")
    public Result<UserResponseDTO> register(@Valid @RequestBody RegisterDTO registerDTO) {
        UserResponseDTO user = authService.register(registerDTO);
        return Result.success(user, "注册成功");
    }

    /**
     * 刷新 Token
     */
    @Operation(summary = "刷新Token", description = "使用有效的 Token 刷新获取新 Token")
    @PostMapping("/refresh")
    public Result<Map<String, String>> refreshToken(@RequestHeader("Authorization") String authHeader) {
        String oldToken = authHeader.replace("Bearer ", "");
        String newToken = authService.refreshToken(oldToken);
        
        Map<String, String> data = new HashMap<>();
        data.put("token", newToken);
        
        return Result.success(data, "Token 刷新成功");
    }

    /**
     * 获取当前用户信息
     */
    @Operation(summary = "获取当前用户", description = "获取当前登录用户的信息")
    @GetMapping("/me")
    public Result<UserResponseDTO> getCurrentUser() {
        UserResponseDTO user = authService.getCurrentUser();
        return Result.success(user, "获取用户信息成功");
    }

    /**
     * 登出（客户端删除 Token 即可）
     */
    @Operation(summary = "登出", description = "用户登出（客户端删除 Token）")
    @PostMapping("/logout")
    public Result<Void> logout() {
        return Result.success(null, "登出成功");
    }
}
