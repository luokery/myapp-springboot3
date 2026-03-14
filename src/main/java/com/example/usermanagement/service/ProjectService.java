package com.example.usermanagement.service;

import com.example.usermanagement.dto.PageDTO;
import com.example.usermanagement.dto.ProjectCreateDTO;
import com.example.usermanagement.dto.ProjectQueryDTO;
import com.example.usermanagement.dto.ProjectResponseDTO;
import com.example.usermanagement.dto.ProjectUpdateDTO;
import com.example.usermanagement.mapper.ProjectMapper;
import com.example.usermanagement.model.Project;
import com.example.usermanagement.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 项目服务
 * 使用 Redis 缓存项目数据
 * 支持乐观锁和逻辑删除
 */
@Slf4j
@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "projects")
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper;

    /**
     * 获取所有项目列表
     * 缓存项目列表数据
     */
    @Cacheable(key = "'all'", unless = "#result == null || #result.isEmpty()")
    public List<ProjectResponseDTO> getAllProjects() {
        log.debug("从数据库加载所有项目");
        return projectMapper.toResponseDTOList(projectRepository.findAll());
    }

    /**
     * 分页查询项目列表
     * 支持多条件查询和排序
     */
    public PageDTO<ProjectResponseDTO> queryProjects(ProjectQueryDTO query) {
        log.debug("分页查询项目: {}", query);

        // 验证日期范围
        if (!query.isValidDateRange()) {
            throw new IllegalArgumentException("日期范围无效：起始日期不能晚于结束日期");
        }

        // 验证分页参数
        if (!query.isValidPagination()) {
            throw new IllegalArgumentException("分页参数无效：页码不能为负数，每页大小必须在1-100之间");
        }

        // 查询数据
        List<Project> projects = projectRepository.findByQuery(query);
        long total = projectRepository.countByQuery(query);

        // 转换为 DTO
        List<ProjectResponseDTO> content = projectMapper.toResponseDTOList(projects);

        return PageDTO.of(content, query.getPageNumber(), query.getPageSize(), total);
    }

    /**
     * 根据ID获取项目
     * 缓存单个项目数据
     */
    @Cacheable(key = "#id", unless = "#result == null")
    public ProjectResponseDTO getProjectById(Long id) {
        log.debug("从数据库加载项目: {}", id);
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("项目不存在，ID: " + id));
        return projectMapper.toResponseDTO(project);
    }

    /**
     * 搜索项目
     * 不缓存搜索结果
     */
    public List<ProjectResponseDTO> searchProjects(String keyword) {
        log.debug("搜索项目: {}", keyword);
        if (!StringUtils.hasText(keyword)) {
            throw new IllegalArgumentException("搜索关键词不能为空");
        }
        return projectMapper.toResponseDTOList(projectRepository.search(keyword.trim()));
    }

    /**
     * 根据状态获取项目
     * 缓存按状态分组的项目列表
     */
    @Cacheable(key = "'status:' + #status", unless = "#result == null || #result.isEmpty()")
    public List<ProjectResponseDTO> getProjectsByStatus(Integer status) {
        log.debug("从数据库加载状态为 {} 的项目", status);
        if (status == null || status < 0 || status > 2) {
            throw new IllegalArgumentException("项目状态无效，有效值为：0-已暂停，1-进行中，2-已完成");
        }
        return projectMapper.toResponseDTOList(projectRepository.findByStatus(status));
    }

    /**
     * 创建项目
     * 清除项目列表缓存
     */
    @Transactional
    @Caching(evict = {
            @CacheEvict(key = "'all'"),
            @CacheEvict(cacheNames = "statistics", key = "'projectTotal'")
    })
    public ProjectResponseDTO createProject(ProjectCreateDTO dto) {
        log.debug("创建项目: {}", dto.getProjectCode());

        // 标准化项目编号
        String projectCode = dto.getProjectCode().trim().toUpperCase();

        // 检查项目编号是否已存在（排除已删除的）
        if (projectRepository.findByProjectCode(projectCode).isPresent()) {
            throw new IllegalArgumentException("项目编号已存在: " + projectCode);
        }

        // 验证日期范围
        if (dto.getStartDate() != null && dto.getEndDate() != null) {
            if (dto.getStartDate().isAfter(dto.getEndDate())) {
                throw new IllegalArgumentException("开始日期不能晚于结束日期");
            }
        }

        Project project = projectMapper.toEntity(dto);
        project.setProjectCode(projectCode);
        projectRepository.insert(project);

        log.info("项目创建成功: ID={}, Code={}", project.getId(), project.getProjectCode());
        return projectMapper.toResponseDTO(project);
    }

    /**
     * 更新项目（乐观锁）
     * 清除该项目的缓存和列表缓存
     */
    @Transactional
    @CacheEvict(allEntries = true)
    public ProjectResponseDTO updateProject(Long id, ProjectUpdateDTO dto) {
        log.debug("更新项目: {}, version: {}", id, dto.getVersion());
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("项目不存在，ID: " + id));

        // 检查乐观锁版本号
        if (dto.getVersion() == null) {
            throw new RuntimeException("更新操作必须提供版本号(version)");
        }
        if (!project.getVersion().equals(dto.getVersion())) {
            log.warn("乐观锁冲突: 项目{}已被其他操作修改，当前版本{}，请求版本{}", 
                    id, project.getVersion(), dto.getVersion());
            throw new RuntimeException("数据已被其他用户修改，请刷新后重试。" +
                    "当前版本: " + project.getVersion() + "，请求版本: " + dto.getVersion());
        }

        // 检查项目编号是否被其他项目占用
        if (dto.getProjectCode() != null && !dto.getProjectCode().trim().isEmpty()) {
            String newCode = dto.getProjectCode().trim().toUpperCase();
            if (!newCode.equals(project.getProjectCode())) {
                projectRepository.findByProjectCode(newCode)
                        .ifPresent(p -> {
                            throw new IllegalArgumentException("项目编号已存在: " + newCode);
                        });
            }
        }

        // 验证日期范围
        LocalDateTime startDate = dto.getStartDate() != null ? dto.getStartDate() : project.getStartDate();
        LocalDateTime endDate = dto.getEndDate() != null ? dto.getEndDate() : project.getEndDate();
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("开始日期不能晚于结束日期");
        }

        projectMapper.updateEntityFromDTO(dto, project);
        
        // 执行更新（带乐观锁检查）
        int rows = projectRepository.update(project);
        if (rows == 0) {
            log.warn("乐观锁更新失败: 项目{}", id);
            throw new RuntimeException("更新失败，数据可能已被其他用户修改或已删除");
        }

        // 重新查询获取最新数据
        project = projectRepository.findById(id).orElseThrow();
        log.info("项目更新成功: ID={}, 新版本={}", id, project.getVersion());
        return projectMapper.toResponseDTO(project);
    }

    /**
     * 更新项目图片（乐观锁）
     * 清除该项目的缓存
     */
    @Transactional
    @CacheEvict(key = "#id")
    public ProjectResponseDTO updateProjectImage(Long id, String imageUrl) {
        log.debug("更新项目图片: {}", id);
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("项目不存在，ID: " + id));

        int rows = projectRepository.updateImage(id, imageUrl, LocalDateTime.now(), project.getVersion());
        if (rows == 0) {
            log.warn("项目图片更新失败（乐观锁冲突）: ID={}", id);
            throw new RuntimeException("更新失败，数据可能已被其他用户修改或已删除");
        }

        // 重新查询获取最新数据
        project = projectRepository.findById(id).orElseThrow();
        log.info("项目图片更新成功: ID={}", id);
        return projectMapper.toResponseDTO(project);
    }

    /**
     * 删除项目（逻辑删除）
     * 清除该项目的缓存和列表缓存
     */
    @Transactional
    @CacheEvict(allEntries = true)
    public void deleteProject(Long id) {
        log.debug("逻辑删除项目: {}", id);
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("项目不存在，ID: " + id));
        
        int rows = projectRepository.deleteById(id);
        if (rows == 0) {
            throw new RuntimeException("删除失败，项目可能已被删除");
        }
        log.info("项目已逻辑删除: ID={}, Code={}", id, project.getProjectCode());
    }

    /**
     * 恢复已删除的项目
     */
    @Transactional
    @CacheEvict(allEntries = true)
    public ProjectResponseDTO restoreProject(Long id) {
        log.debug("恢复项目: {}", id);
        Project project = projectRepository.findByIdIncludeDeleted(id)
                .orElseThrow(() -> new RuntimeException("项目不存在，ID: " + id));
        
        if (!Boolean.TRUE.equals(project.getDeleted())) {
            throw new RuntimeException("项目未被删除，无需恢复");
        }
        
        int rows = projectRepository.restore(id);
        if (rows == 0) {
            throw new RuntimeException("恢复失败");
        }
        
        project = projectRepository.findById(id).orElseThrow();
        log.info("项目已恢复: ID={}, Code={}", id, project.getProjectCode());
        return projectMapper.toResponseDTO(project);
    }

    /**
     * 获取项目总数
     * 缓存统计数据
     */
    @Cacheable(cacheNames = "statistics", key = "'projectTotal'")
    public long getProjectCount() {
        log.debug("统计项目数量");
        return projectRepository.count();
    }

    /**
     * 获取指定状态的项目数量
     * 缓存统计数据
     */
    @Cacheable(cacheNames = "statistics", key = "'projectStatus:' + #status")
    public long getProjectCountByStatus(Integer status) {
        log.debug("统计状态 {} 的项目数量", status);
        return projectRepository.countByStatus(status);
    }
}
