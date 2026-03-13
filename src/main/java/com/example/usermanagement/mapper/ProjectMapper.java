package com.example.usermanagement.mapper;

import com.example.usermanagement.dto.ProjectCreateDTO;
import com.example.usermanagement.dto.ProjectResponseDTO;
import com.example.usermanagement.dto.ProjectUpdateDTO;
import com.example.usermanagement.model.Project;
import org.mapstruct.*;
import java.util.List;

@Mapper(componentModel = "spring")
public interface ProjectMapper {
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "imageUrl", ignore = true)
    @Mapping(target = "status", expression = "java(dto.getStatus() != null ? dto.getStatus() : 1)")
    @Mapping(target = "createdAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "updatedAt", expression = "java(java.time.LocalDateTime.now())")
    Project toEntity(ProjectCreateDTO dto);
    
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "updatedAt", expression = "java(java.time.LocalDateTime.now())")
    void updateEntityFromDTO(ProjectUpdateDTO dto, @MappingTarget Project entity);
    
    @Mapping(target = "statusName", expression = "java(getStatusName(entity.getStatus()))")
    ProjectResponseDTO toResponseDTO(Project entity);
    
    List<ProjectResponseDTO> toResponseDTOList(List<Project> entities);
    
    default String getStatusName(Integer status) {
        if (status == null) return "未知";
        return switch (status) {
            case 1 -> "进行中";
            case 2 -> "已完成";
            case 0 -> "已暂停";
            default -> "未知";
        };
    }
}
