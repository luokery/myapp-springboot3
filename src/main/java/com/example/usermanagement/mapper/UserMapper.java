package com.example.usermanagement.mapper;

import com.example.usermanagement.dto.UserCreateDTO;
import com.example.usermanagement.dto.UserResponseDTO;
import com.example.usermanagement.dto.UserUpdateDTO;
import com.example.usermanagement.dto.RegisterDTO;
import com.example.usermanagement.model.User;
import org.mapstruct.*;
import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "status", constant = "1")
    @Mapping(target = "createdAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "updatedAt", expression = "java(java.time.LocalDateTime.now())")
    User toEntity(UserCreateDTO dto);
    
    /**
     * 从注册 DTO 转换为实体（包含密码）
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", constant = "1")
    @Mapping(target = "role", constant = "user")
    @Mapping(target = "createdAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "updatedAt", expression = "java(java.time.LocalDateTime.now())")
    User toEntityFromRegister(RegisterDTO dto);
    
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "updatedAt", expression = "java(java.time.LocalDateTime.now())")
    void updateEntityFromDTO(UserUpdateDTO dto, @MappingTarget User entity);
    
    UserResponseDTO toResponseDTO(User entity);
    
    List<UserResponseDTO> toResponseDTOList(List<User> entities);
}
