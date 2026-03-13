package com.example.usermanagement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文件上传响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "文件上传响应")
public class FileUploadResponseDTO {
    
    @Schema(description = "文件ID")
    private Long id;
    
    @Schema(description = "原始文件名")
    private String originalFilename;
    
    @Schema(description = "存储文件名")
    private String storedFilename;
    
    @Schema(description = "访问URL")
    private String accessUrl;
    
    @Schema(description = "文件大小（字节）")
    private Long fileSize;
    
    @Schema(description = "文件类型")
    private String contentType;
    
    @Schema(description = "访问类型：PUBLIC/PRIVATE")
    private String accessType;
    
    @Schema(description = "业务类型")
    private String businessType;
}
