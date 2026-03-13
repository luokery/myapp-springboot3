package com.example.usermanagement.repository;

import com.example.usermanagement.model.User;
import org.apache.ibatis.annotations.*;
import java.util.List;
import java.util.Optional;

@Mapper
public interface UserRepository {
    
    @Select("SELECT * FROM users ORDER BY created_at DESC")
    List<User> findAll();
    
    @Select("SELECT * FROM users WHERE id = #{id}")
    Optional<User> findById(Long id);
    
    @Select("SELECT * FROM users WHERE username LIKE CONCAT('%', #{keyword}, '%') OR email LIKE CONCAT('%', #{keyword}, '%')")
    List<User> search(String keyword);
    
    @Insert("INSERT INTO users (username, password, email, phone, age, role, status, created_at, updated_at) " +
            "VALUES (#{username}, #{password}, #{email}, #{phone}, #{age}, #{role}, #{status}, #{createdAt}, #{updatedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(User user);
    
    @Update("UPDATE users SET username = #{username}, password = #{password}, email = #{email}, phone = #{phone}, " +
            "age = #{age}, role = #{role}, status = #{status}, updated_at = #{updatedAt} WHERE id = #{id}")
    int update(User user);
    
    @Delete("DELETE FROM users WHERE id = #{id}")
    int deleteById(Long id);
    
    @Select("SELECT COUNT(*) FROM users")
    long count();
    
    @Select("SELECT * FROM users WHERE username = #{username}")
    Optional<User> findByUsername(String username);
    
    @Select("SELECT * FROM users WHERE email = #{email}")
    Optional<User> findByEmail(String email);
}
