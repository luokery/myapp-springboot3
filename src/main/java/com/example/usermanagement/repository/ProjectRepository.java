package com.example.usermanagement.repository;

import com.example.usermanagement.model.Project;
import org.apache.ibatis.annotations.*;
import java.util.List;
import java.util.Optional;

@Mapper
public interface ProjectRepository {
    
    @Select("SELECT * FROM projects ORDER BY created_at DESC")
    List<Project> findAll();
    
    @Select("SELECT * FROM projects WHERE id = #{id}")
    Optional<Project> findById(Long id);
    
    @Select("SELECT * FROM projects WHERE project_code = #{projectCode}")
    Optional<Project> findByProjectCode(String projectCode);
    
    @Select("SELECT * FROM projects WHERE project_name LIKE CONCAT('%', #{keyword}, '%') OR project_code LIKE CONCAT('%', #{keyword}, '%')")
    List<Project> search(String keyword);
    
    @Select("SELECT * FROM projects WHERE status = #{status} ORDER BY created_at DESC")
    List<Project> findByStatus(Integer status);
    
    @Insert("INSERT INTO projects (project_code, project_name, description, image_url, status, start_date, end_date, created_at, updated_at) " +
            "VALUES (#{projectCode}, #{projectName}, #{description}, #{imageUrl}, #{status}, #{startDate}, #{endDate}, #{createdAt}, #{updatedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Project project);
    
    @Update("UPDATE projects SET project_code = #{projectCode}, project_name = #{projectName}, " +
            "description = #{description}, image_url = #{imageUrl}, status = #{status}, " +
            "start_date = #{startDate}, end_date = #{endDate}, updated_at = #{updatedAt} WHERE id = #{id}")
    int update(Project project);
    
    @Delete("DELETE FROM projects WHERE id = #{id}")
    int deleteById(Long id);
    
    @Select("SELECT COUNT(*) FROM projects")
    long count();
    
    @Select("SELECT COUNT(*) FROM projects WHERE status = #{status}")
    long countByStatus(Integer status);
}
