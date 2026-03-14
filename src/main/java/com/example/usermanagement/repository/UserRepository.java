package com.example.usermanagement.repository;

import com.example.usermanagement.model.User;
import org.apache.ibatis.annotations.*;
import java.util.List;
import java.util.Optional;

/**
 * 用户数据访问层
 * 支持乐观锁和逻辑删除
 */
@Mapper
public interface UserRepository {
    
    /**
     * 查询所有未删除的用户
     */
    @Select("SELECT * FROM users WHERE deleted = false ORDER BY created_at DESC")
    List<User> findAll();
    
    /**
     * 根据ID查询未删除的用户
     */
    @Select("SELECT * FROM users WHERE id = #{id} AND deleted = false")
    Optional<User> findById(Long id);
    
    /**
     * 根据ID查询用户（包含已删除的，用于内部操作）
     */
    @Select("SELECT * FROM users WHERE id = #{id}")
    Optional<User> findByIdIncludeDeleted(Long id);
    
    /**
     * 搜索用户（排除已删除）
     */
    @Select("SELECT * FROM users WHERE deleted = false AND (username LIKE CONCAT('%', #{keyword}, '%') OR email LIKE CONCAT('%', #{keyword}, '%'))")
    List<User> search(String keyword);
    
    /**
     * 插入用户
     * 初始化version=1, deleted=false
     */
    @Insert("INSERT INTO users (username, password, email, phone, age, role, status, created_at, updated_at, version, deleted) " +
            "VALUES (#{username}, #{password}, #{email}, #{phone}, #{age}, #{role}, #{status}, #{createdAt}, #{updatedAt}, 1, false)")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(User user);
    
    /**
     * 更新用户（乐观锁）
     * 更新时检查version，成功后version+1
     */
    @Update("UPDATE users SET username = #{username}, password = #{password}, email = #{email}, phone = #{phone}, " +
            "age = #{age}, role = #{role}, status = #{status}, updated_at = #{updatedAt}, version = version + 1 " +
            "WHERE id = #{id} AND version = #{version} AND deleted = false")
    int update(User user);
    
    /**
     * 逻辑删除用户
     */
    @Update("UPDATE users SET deleted = true, updated_at = NOW() WHERE id = #{id} AND deleted = false")
    int deleteById(Long id);
    
    /**
     * 物理删除用户（仅用于清理数据，谨慎使用）
     */
    @Delete("DELETE FROM users WHERE id = #{id}")
    int forceDelete(Long id);
    
    /**
     * 统计未删除用户数量
     */
    @Select("SELECT COUNT(*) FROM users WHERE deleted = false")
    long count();
    
    /**
     * 根据用户名查询（排除已删除）
     */
    @Select("SELECT * FROM users WHERE username = #{username} AND deleted = false")
    Optional<User> findByUsername(String username);
    
    /**
     * 根据邮箱查询（排除已删除）
     */
    @Select("SELECT * FROM users WHERE email = #{email} AND deleted = false")
    Optional<User> findByEmail(String email);
    
    /**
     * 根据用户名查询（包含已删除，用于注册时检查唯一性）
     */
    @Select("SELECT * FROM users WHERE username = #{username}")
    Optional<User> findByUsernameIncludeDeleted(String username);
    
    /**
     * 根据邮箱查询（包含已删除，用于注册时检查唯一性）
     */
    @Select("SELECT * FROM users WHERE email = #{email}")
    Optional<User> findByEmailIncludeDeleted(String email);
    
    /**
     * 恢复已删除的用户
     */
    @Update("UPDATE users SET deleted = false, updated_at = NOW() WHERE id = #{id} AND deleted = true")
    int restore(Long id);
}
