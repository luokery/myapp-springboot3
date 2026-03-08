package com.example.usermanagement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Schema(description = "项目响应数据")
public class ProjectResponseDTO {
    
    @Schema(description = "项目ID", example = "1")
    private Long id;
    
    @Schema(description = "项目编号", example = "PRJ-2024-001")
    private String projectCode;
    
    @Schema(description = "项目名称", example = "智慧城市项目")
    private String projectName;
    
    @Schema(description = "项目描述")
    private String description;
    
    @Schema(description = "项目图片URL")
    private String imageUrl;
    
    @Schema(description = "状态：1-进行中，2-已完成，0-已暂停")
    private Integer status;
    
    @Schema(description = "状态名称")
    private String statusName;
    
    @Schema(description = "开始日期")
    private LocalDateTime startDate;
    
    @Schema(description = "结束日期")
    private LocalDateTime endDate;
    
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
    
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
