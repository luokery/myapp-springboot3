package com.example.usermanagement.repository;

import com.example.usermanagement.dto.ProjectQueryDTO;
import com.example.usermanagement.model.Project;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.session.SqlSession;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 项目数据访问层
 * 支持乐观锁和逻辑删除
 */
@Mapper
@Repository
public interface ProjectRepository {

    /**
     * 查询所有未删除的项目
     */
    @Select("SELECT * FROM projects WHERE deleted = false ORDER BY created_at DESC")
    List<Project> findAll();

    /**
     * 根据ID查询未删除的项目
     */
    @Select("SELECT * FROM projects WHERE id = #{id} AND deleted = false")
    Optional<Project> findById(Long id);

    /**
     * 根据ID查询项目（包含已删除的，用于内部操作）
     */
    @Select("SELECT * FROM projects WHERE id = #{id}")
    Optional<Project> findByIdIncludeDeleted(Long id);

    /**
     * 根据项目编号查询（排除已删除）
     */
    @Select("SELECT * FROM projects WHERE project_code = #{projectCode} AND deleted = false")
    Optional<Project> findByProjectCode(String projectCode);

    /**
     * 搜索项目（排除已删除）
     */
    @Select("SELECT * FROM projects WHERE deleted = false AND (project_name LIKE CONCAT('%', #{keyword}, '%') OR project_code LIKE CONCAT('%', #{keyword}, '%'))")
    List<Project> search(String keyword);

    /**
     * 根据状态查询项目（排除已删除）
     */
    @Select("SELECT * FROM projects WHERE status = #{status} AND deleted = false ORDER BY created_at DESC")
    List<Project> findByStatus(Integer status);

    /**
     * 插入项目
     * 初始化version=1, deleted=false
     */
    @Insert("INSERT INTO projects (project_code, project_name, description, image_url, status, start_date, end_date, created_at, updated_at, version, deleted) " +
            "VALUES (#{projectCode}, #{projectName}, #{description}, #{imageUrl}, #{status}, #{startDate}, #{endDate}, #{createdAt}, #{updatedAt}, 1, false)")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Project project);

    /**
     * 更新项目（乐观锁）
     * 更新时检查version，成功后version+1
     */
    @Update("UPDATE projects SET project_code = #{projectCode}, project_name = #{projectName}, " +
            "description = #{description}, image_url = #{imageUrl}, status = #{status}, " +
            "start_date = #{startDate}, end_date = #{endDate}, updated_at = #{updatedAt}, version = version + 1 " +
            "WHERE id = #{id} AND version = #{version} AND deleted = false")
    int update(Project project);

    /**
     * 更新项目图片（乐观锁）
     */
    @Update("UPDATE projects SET image_url = #{imageUrl}, updated_at = #{updatedAt}, version = version + 1 " +
            "WHERE id = #{id} AND version = #{version} AND deleted = false")
    int updateImage(@Param("id") Long id, @Param("imageUrl") String imageUrl, 
                    @Param("updatedAt") java.time.LocalDateTime updatedAt, @Param("version") Integer version);

    /**
     * 逻辑删除项目
     */
    @Update("UPDATE projects SET deleted = true, updated_at = NOW() WHERE id = #{id} AND deleted = false")
    int deleteById(Long id);

    /**
     * 物理删除项目（仅用于清理数据，谨慎使用）
     */
    @Delete("DELETE FROM projects WHERE id = #{id}")
    int forceDelete(Long id);

    /**
     * 统计未删除项目数量
     */
    @Select("SELECT COUNT(*) FROM projects WHERE deleted = false")
    long count();

    /**
     * 统计未删除项目按状态分组数量
     */
    @Select("SELECT COUNT(*) FROM projects WHERE status = #{status} AND deleted = false")
    long countByStatus(Integer status);

    /**
     * 分页查询项目列表（带多条件，排除已删除）
     */
    List<Project> findByQuery(ProjectQueryDTO query);

    /**
     * 统计查询结果数量（排除已删除）
     */
    long countByQuery(ProjectQueryDTO query);

    /**
     * 恢复已删除的项目
     */
    @Update("UPDATE projects SET deleted = false, updated_at = NOW() WHERE id = #{id} AND deleted = true")
    int restore(Long id);
}
