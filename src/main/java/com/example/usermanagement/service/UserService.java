package com.example.usermanagement.service;

import com.example.usermanagement.dto.UserCreateDTO;
import com.example.usermanagement.dto.UserResponseDTO;
import com.example.usermanagement.dto.UserUpdateDTO;
import com.example.usermanagement.mapper.UserMapper;
import com.example.usermanagement.model.User;
import com.example.usermanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 用户服务
 * 使用 Redis 缓存用户数据
 */
@Slf4j
@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "users")
public class UserService {
    
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    
    /**
     * 获取所有用户列表
     * 缓存用户列表数据
     */
    @Cacheable(key = "'all'", unless = "#result == null || #result.isEmpty()")
    public List<UserResponseDTO> getAllUsers() {
        log.debug("从数据库加载所有用户");
        return userMapper.toResponseDTOList(userRepository.findAll());
    }
    
    /**
     * 根据ID获取用户
     * 缓存单个用户数据
     */
    @Cacheable(key = "#id", unless = "#result == null")
    public UserResponseDTO getUserById(Long id) {
        log.debug("从数据库加载用户: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在，ID: " + id));
        return userMapper.toResponseDTO(user);
    }
    
    /**
     * 搜索用户
     * 不缓存搜索结果（关键词多变）
     */
    public List<UserResponseDTO> searchUsers(String keyword) {
        log.debug("搜索用户: {}", keyword);
        return userMapper.toResponseDTOList(userRepository.search(keyword));
    }
    
    /**
     * 创建用户
     * 清除用户列表缓存
     */
    @Transactional
    @CacheEvict(key = "'all'")
    public UserResponseDTO createUser(UserCreateDTO dto) {
        log.debug("创建用户: {}", dto.getUsername());
        // 检查用户名是否已存在
        if (userRepository.findByUsername(dto.getUsername()).isPresent()) {
            throw new RuntimeException("用户名已存在: " + dto.getUsername());
        }
        // 检查邮箱是否已存在
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new RuntimeException("邮箱已被注册: " + dto.getEmail());
        }
        
        User user = userMapper.toEntity(dto);
        userRepository.insert(user);
        return userMapper.toResponseDTO(user);
    }
    
    /**
     * 更新用户
     * 清除该用户的缓存和列表缓存
     */
    @Transactional
    @Caching(evict = {
            @CacheEvict(key = "#id"),
            @CacheEvict(key = "'all'")
    })
    public UserResponseDTO updateUser(Long id, UserUpdateDTO dto) {
        log.debug("更新用户: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在，ID: " + id));
        
        // 检查用户名是否被其他用户占用
        if (dto.getUsername() != null && !dto.getUsername().equals(user.getUsername())) {
            userRepository.findByUsername(dto.getUsername())
                    .ifPresent(u -> {
                        throw new RuntimeException("用户名已存在: " + dto.getUsername());
                    });
        }
        
        // 检查邮箱是否被其他用户占用
        if (dto.getEmail() != null && !dto.getEmail().equals(user.getEmail())) {
            userRepository.findByEmail(dto.getEmail())
                    .ifPresent(u -> {
                        throw new RuntimeException("邮箱已被注册: " + dto.getEmail());
                    });
        }
        
        userMapper.updateEntityFromDTO(dto, user);
        userRepository.update(user);
        return userMapper.toResponseDTO(user);
    }
    
    /**
     * 删除用户
     * 清除该用户的缓存和列表缓存
     */
    @Transactional
    @Caching(evict = {
            @CacheEvict(key = "#id"),
            @CacheEvict(key = "'all'")
    })
    public void deleteUser(Long id) {
        log.debug("删除用户: {}", id);
        if (userRepository.findById(id).isEmpty()) {
            throw new RuntimeException("用户不存在，ID: " + id);
        }
        userRepository.deleteById(id);
    }
    
    /**
     * 获取用户总数
     * 缓存统计数据
     */
    @Cacheable(cacheNames = "statistics", key = "'userCount'")
    public long getUserCount() {
        log.debug("统计用户数量");
        return userRepository.count();
    }
}
