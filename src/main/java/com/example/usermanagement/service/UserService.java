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
 * 支持乐观锁和逻辑删除
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
        // 检查用户名是否已存在（包含已删除的）
        if (userRepository.findByUsernameIncludeDeleted(dto.getUsername()).isPresent()) {
            throw new RuntimeException("用户名已存在: " + dto.getUsername());
        }
        // 检查邮箱是否已存在（包含已删除的）
        if (userRepository.findByEmailIncludeDeleted(dto.getEmail()).isPresent()) {
            throw new RuntimeException("邮箱已被注册: " + dto.getEmail());
        }
        
        User user = userMapper.toEntity(dto);
        userRepository.insert(user);
        return userMapper.toResponseDTO(user);
    }
    
    /**
     * 更新用户（乐观锁）
     * 清除该用户的缓存和列表缓存
     */
    @Transactional
    @Caching(evict = {
            @CacheEvict(key = "#id"),
            @CacheEvict(key = "'all'")
    })
    public UserResponseDTO updateUser(Long id, UserUpdateDTO dto) {
        log.debug("更新用户: {}, version: {}", id, dto.getVersion());
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在，ID: " + id));
        
        // 检查乐观锁版本号
        if (dto.getVersion() == null) {
            throw new RuntimeException("更新操作必须提供版本号(version)");
        }
        if (!user.getVersion().equals(dto.getVersion())) {
            log.warn("乐观锁冲突: 用户{}已被其他操作修改，当前版本{}，请求版本{}", 
                    id, user.getVersion(), dto.getVersion());
            throw new RuntimeException("数据已被其他用户修改，请刷新后重试。" +
                    "当前版本: " + user.getVersion() + "，请求版本: " + dto.getVersion());
        }
        
        // 检查用户名是否被其他用户占用
        if (dto.getUsername() != null && !dto.getUsername().equals(user.getUsername())) {
            userRepository.findByUsernameIncludeDeleted(dto.getUsername())
                    .ifPresent(u -> {
                        throw new RuntimeException("用户名已存在: " + dto.getUsername());
                    });
        }
        
        // 检查邮箱是否被其他用户占用
        if (dto.getEmail() != null && !dto.getEmail().equals(user.getEmail())) {
            userRepository.findByEmailIncludeDeleted(dto.getEmail())
                    .ifPresent(u -> {
                        throw new RuntimeException("邮箱已被注册: " + dto.getEmail());
                    });
        }
        
        userMapper.updateEntityFromDTO(dto, user);
        
        // 执行更新（带乐观锁检查）
        int rows = userRepository.update(user);
        if (rows == 0) {
            log.warn("乐观锁更新失败: 用户{}", id);
            throw new RuntimeException("更新失败，数据可能已被其他用户修改或已删除");
        }
        
        // 重新查询获取最新数据
        user = userRepository.findById(id).orElseThrow();
        log.info("用户更新成功: ID={}, 新版本={}", id, user.getVersion());
        return userMapper.toResponseDTO(user);
    }
    
    /**
     * 删除用户（逻辑删除）
     * 清除该用户的缓存和列表缓存
     */
    @Transactional
    @Caching(evict = {
            @CacheEvict(key = "#id"),
            @CacheEvict(key = "'all'")
    })
    public void deleteUser(Long id) {
        log.debug("逻辑删除用户: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在，ID: " + id));
        
        int rows = userRepository.deleteById(id);
        if (rows == 0) {
            throw new RuntimeException("删除失败，用户可能已被删除");
        }
        log.info("用户已逻辑删除: ID={}, username={}", id, user.getUsername());
    }
    
    /**
     * 恢复已删除的用户
     */
    @Transactional
    @CacheEvict(allEntries = true)
    public UserResponseDTO restoreUser(Long id) {
        log.debug("恢复用户: {}", id);
        User user = userRepository.findByIdIncludeDeleted(id)
                .orElseThrow(() -> new RuntimeException("用户不存在，ID: " + id));
        
        if (!Boolean.TRUE.equals(user.getDeleted())) {
            throw new RuntimeException("用户未被删除，无需恢复");
        }
        
        int rows = userRepository.restore(id);
        if (rows == 0) {
            throw new RuntimeException("恢复失败");
        }
        
        user = userRepository.findById(id).orElseThrow();
        log.info("用户已恢复: ID={}, username={}", id, user.getUsername());
        return userMapper.toResponseDTO(user);
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
    
    /**
     * 根据用户名查找用户（用于认证）
     * 不缓存，每次都从数据库获取最新数据
     */
    public User findByUsername(String username) {
        log.debug("根据用户名查找用户: {}", username);
        return userRepository.findByUsername(username).orElse(null);
    }
}
