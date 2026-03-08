package com.example.usermanagement.service;

import com.example.usermanagement.dto.ProjectCreateDTO;
import com.example.usermanagement.dto.ProjectResponseDTO;
import com.example.usermanagement.dto.ProjectUpdateDTO;
import com.example.usermanagement.mapper.ProjectMapper;
import com.example.usermanagement.model.Project;
import com.example.usermanagement.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectService {
    
    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper;
    
    public List<ProjectResponseDTO> getAllProjects() {
        return projectMapper.toResponseDTOList(projectRepository.findAll());
    }
    
    public ProjectResponseDTO getProjectById(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("项目不存在，ID: " + id));
        return projectMapper.toResponseDTO(project);
    }
    
    public List<ProjectResponseDTO> searchProjects(String keyword) {
        return projectMapper.toResponseDTOList(projectRepository.search(keyword));
    }
    
    public List<ProjectResponseDTO> getProjectsByStatus(Integer status) {
        return projectMapper.toResponseDTOList(projectRepository.findByStatus(status));
    }
    
    @Transactional
    public ProjectResponseDTO createProject(ProjectCreateDTO dto) {
        // 检查项目编号是否已存在
        if (projectRepository.findByProjectCode(dto.getProjectCode()).isPresent()) {
            throw new RuntimeException("项目编号已存在: " + dto.getProjectCode());
        }
        
        Project project = projectMapper.toEntity(dto);
        projectRepository.insert(project);
        return projectMapper.toResponseDTO(project);
    }
    
    @Transactional
    public ProjectResponseDTO updateProject(Long id, ProjectUpdateDTO dto) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("项目不存在，ID: " + id));
        
        // 检查项目编号是否被其他项目占用
        if (dto.getProjectCode() != null && !dto.getProjectCode().equals(project.getProjectCode())) {
            projectRepository.findByProjectCode(dto.getProjectCode())
                    .ifPresent(p -> {
                        throw new RuntimeException("项目编号已存在: " + dto.getProjectCode());
                    });
        }
        
        projectMapper.updateEntityFromDTO(dto, project);
        projectRepository.update(project);
        return projectMapper.toResponseDTO(project);
    }
    
    @Transactional
    public ProjectResponseDTO updateProjectImage(Long id, String imageUrl) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("项目不存在，ID: " + id));
        
        project.setImageUrl(imageUrl);
        project.setUpdatedAt(java.time.LocalDateTime.now());
        projectRepository.update(project);
        return projectMapper.toResponseDTO(project);
    }
    
    @Transactional
    public void deleteProject(Long id) {
        if (projectRepository.findById(id).isEmpty()) {
            throw new RuntimeException("项目不存在，ID: " + id);
        }
        projectRepository.deleteById(id);
    }
    
    public long getProjectCount() {
        return projectRepository.count();
    }
    
    public long getProjectCountByStatus(Integer status) {
        return projectRepository.countByStatus(status);
    }
}
