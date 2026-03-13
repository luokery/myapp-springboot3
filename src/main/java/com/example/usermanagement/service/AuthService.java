package com.example.usermanagement.service;

import com.example.usermanagement.dto.LoginDTO;
import com.example.usermanagement.dto.RegisterDTO;
import com.example.usermanagement.dto.UserResponseDTO;
import com.example.usermanagement.mapper.UserMapper;
import com.example.usermanagement.model.User;
import com.example.usermanagement.repository.UserRepository;
import com.example.usermanagement.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 认证服务
 * 处理登录、注册、Token 刷新等业务逻辑
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * 用户登录
     *
     * @param loginDTO 登录信息
     * @return JWT Token
     */
    public String login(LoginDTO loginDTO) {
        log.debug("用户登录: {}", loginDTO.getUsername());

        // 查找用户
        User user = userRepository.findByUsername(loginDTO.getUsername())
                .orElseThrow(() -> new RuntimeException("用户名或密码错误"));

        log.debug("用户找到: {}, 存储密码: {}, 输入密码: {}", 
                user.getUsername(), user.getPassword(), loginDTO.getPassword());

        // 验证密码
        boolean matches = passwordEncoder.matches(loginDTO.getPassword(), user.getPassword());
        log.debug("密码匹配结果: {}", matches);
        
        if (!matches) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 检查用户状态
        if (user.getStatus() != 1) {
            throw new RuntimeException("用户已被禁用");
        }

        // 生成 Token
        return jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
    }

    /**
     * 用户注册
     *
     * @param registerDTO 注册信息
     * @return 用户信息
     */
    @Transactional
    public UserResponseDTO register(RegisterDTO registerDTO) {
        log.debug("用户注册: {}", registerDTO.getUsername());

        // 检查用户名是否已存在
        if (userRepository.findByUsername(registerDTO.getUsername()).isPresent()) {
            throw new RuntimeException("用户名已存在");
        }

        // 检查邮箱是否已存在
        if (userRepository.findByEmail(registerDTO.getEmail()).isPresent()) {
            throw new RuntimeException("邮箱已被注册");
        }

        // 创建用户
        User user = new User();
        user.setUsername(registerDTO.getUsername());
        String encodedPassword = passwordEncoder.encode(registerDTO.getPassword());
        log.debug("注册用户: {}, 原始密码: {}, 加密后密码: {}", 
                registerDTO.getUsername(), registerDTO.getPassword(), encodedPassword);
        user.setPassword(encodedPassword);
        user.setEmail(registerDTO.getEmail());
        user.setPhone(registerDTO.getPhone());
        user.setAge(registerDTO.getAge());
        user.setRole("user"); // 默认角色
        user.setStatus(1); // 默认状态为启用
        user.setCreatedAt(java.time.LocalDateTime.now());
        user.setUpdatedAt(java.time.LocalDateTime.now());

        userRepository.insert(user);
        log.info("用户注册成功: {}", user.getUsername());

        return userMapper.toResponseDTO(user);
    }

    /**
     * 刷新 Token
     *
     * @param oldToken 旧 Token
     * @return 新 Token
     */
    public String refreshToken(String oldToken) {
        if (!jwtUtil.validateToken(oldToken)) {
            throw new RuntimeException("Token 无效或已过期");
        }
        
        String newToken = jwtUtil.refreshToken(oldToken);
        if (newToken == null) {
            throw new RuntimeException("Token 刷新失败");
        }
        
        return newToken;
    }

    /**
     * 根据用户名获取用户信息
     *
     * @param username 用户名
     * @return 用户信息
     */
    public UserResponseDTO getCurrentUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        return userMapper.toResponseDTO(user);
    }

    /**
     * 获取当前登录用户信息（从 Shiro 上下文获取）
     *
     * @return 用户信息
     */
    public UserResponseDTO getCurrentUser() {
        // 从 Shiro 获取当前认证的用户名
        Object principal = SecurityUtils.getSubject().getPrincipal();
        if (principal == null) {
            throw new RuntimeException("用户未登录");
        }

        // principal 是 JWT Token，从中获取用户名
        String username = jwtUtil.getUsernameFromToken(principal.toString());
        if (username == null) {
            throw new RuntimeException("无效的认证信息");
        }

        return getCurrentUser(username);
    }

    /**
     * 加密密码（用于初始化数据或密码重置）
     *
     * @param rawPassword 原始密码
     * @return 加密后的密码
     */
    public String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }
}
