package com.example.usermanagement.controller;

import com.example.usermanagement.dto.UserCreateDTO;
import com.example.usermanagement.dto.UserResponseDTO;
import com.example.usermanagement.dto.UserUpdateDTO;
import com.example.usermanagement.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "用户管理", description = "用户的增删改查接口")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UserController {
    
    private final UserService userService;
    
    @Operation(summary = "获取用户列表", description = "获取所有用户的列表信息")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "成功获取用户列表")
    })
    @GetMapping
    public ResponseEntity<Result<List<UserResponseDTO>>> getAllUsers() {
        List<UserResponseDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(Result.success(users, "获取用户列表成功"));
    }
    
    @Operation(summary = "获取用户详情", description = "根据用户ID获取用户详细信息")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "成功获取用户详情"),
            @ApiResponse(responseCode = "400", description = "用户不存在")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Result<UserResponseDTO>> getUserById(
            @Parameter(description = "用户ID", required = true) @PathVariable Long id) {
        UserResponseDTO user = userService.getUserById(id);
        return ResponseEntity.ok(Result.success(user, "获取用户详情成功"));
    }
    
    @Operation(summary = "搜索用户", description = "根据关键词搜索用户（支持用户名和邮箱模糊匹配）")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "搜索成功")
    })
    @GetMapping("/search")
    public ResponseEntity<Result<List<UserResponseDTO>>> searchUsers(
            @Parameter(description = "搜索关键词", required = true) @RequestParam String keyword) {
        List<UserResponseDTO> users = userService.searchUsers(keyword);
        return ResponseEntity.ok(Result.success(users, "搜索用户成功"));
    }
    
    @Operation(summary = "创建用户", description = "创建新用户")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "用户创建成功"),
            @ApiResponse(responseCode = "400", description = "参数验证失败或用户名/邮箱已存在")
    })
    @PostMapping
    public ResponseEntity<Result<UserResponseDTO>> createUser(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "用户创建信息", required = true)
            @Valid @RequestBody UserCreateDTO dto) {
        UserResponseDTO user = userService.createUser(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Result.success(user, "创建用户成功"));
    }
    
    @Operation(summary = "更新用户", description = "根据用户ID更新用户信息")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "用户更新成功"),
            @ApiResponse(responseCode = "400", description = "用户不存在或参数验证失败")
    })
    @PutMapping("/{id}")
    public ResponseEntity<Result<UserResponseDTO>> updateUser(
            @Parameter(description = "用户ID", required = true) @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "用户更新信息", required = true)
            @Valid @RequestBody UserUpdateDTO dto) {
        UserResponseDTO user = userService.updateUser(id, dto);
        return ResponseEntity.ok(Result.success(user, "更新用户成功"));
    }
    
    @Operation(summary = "删除用户", description = "根据用户ID删除用户")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "用户删除成功"),
            @ApiResponse(responseCode = "400", description = "用户不存在")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Result<Void>> deleteUser(
            @Parameter(description = "用户ID", required = true) @PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(Result.success(null, "删除用户成功"));
    }
    
    @Operation(summary = "获取用户数量", description = "获取系统中用户的总数")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "成功获取用户数量")
    })
    @GetMapping("/count")
    public ResponseEntity<Result<Map<String, Long>>> getUserCount() {
        Map<String, Long> data = new HashMap<>();
        data.put("count", userService.getUserCount());
        return ResponseEntity.ok(Result.success(data, "获取用户数量成功"));
    }
}
