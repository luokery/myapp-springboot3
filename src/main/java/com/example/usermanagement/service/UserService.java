package com.example.usermanagement.service;

import com.example.usermanagement.dto.UserCreateDTO;
import com.example.usermanagement.dto.UserResponseDTO;
import com.example.usermanagement.dto.UserUpdateDTO;
import com.example.usermanagement.mapper.UserMapper;
import com.example.usermanagement.model.User;
import com.example.usermanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    
    public List<UserResponseDTO> getAllUsers() {
        return userMapper.toResponseDTOList(userRepository.findAll());
    }
    
    public UserResponseDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在，ID: " + id));
        return userMapper.toResponseDTO(user);
    }
    
    public List<UserResponseDTO> searchUsers(String keyword) {
        return userMapper.toResponseDTOList(userRepository.search(keyword));
    }
    
    @Transactional
    public UserResponseDTO createUser(UserCreateDTO dto) {
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
    
    @Transactional
    public UserResponseDTO updateUser(Long id, UserUpdateDTO dto) {
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
    
    @Transactional
    public void deleteUser(Long id) {
        if (userRepository.findById(id).isEmpty()) {
            throw new RuntimeException("用户不存在，ID: " + id);
        }
        userRepository.deleteById(id);
    }
    
    public long getUserCount() {
        return userRepository.count();
    }
}
