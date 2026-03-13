package com.example.usermanagement.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 文件信息实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileInfo {
    
    /**
     * 文件ID
     */
    private Long id;
    
    /**
     * 原始文件名
     */
    private String originalFilename;
    
    /**
     * 存储文件名（UUID）
     */
    private String storedFilename;
    
    /**
     * 文件路径（相对路径）
     */
    private String filePath;
    
    /**
     * 访问URL
     */
    private String accessUrl;
    
    /**
     * 文件大小（字节）
     */
    private Long fileSize;
    
    /**
     * 文件类型（MIME类型）
     */
    private String contentType;
    
    /**
     * 文件扩展名
     */
    private String extension;
    
    /**
     * 访问类型：PUBLIC/PRIVATE
     */
    private FileAccessType accessType;
    
    /**
     * 业务类型（如：avatar, document, project-image等）
     */
    private String businessType;
    
    /**
     * 关联业务ID（如用户ID、项目ID等）
     */
    private Long businessId;
    
    /**
     * 上传用户ID
     */
    private Long uploaderId;
    
    /**
     * 上传时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 是否已删除
     */
    private Boolean deleted;
}
