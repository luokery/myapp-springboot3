package com.example.usermanagement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Schema(description = "项目更新请求")
public class ProjectUpdateDTO {
    
    @Schema(description = "项目编号", example = "PRJ-2024-001")
    @Size(max = 50, message = "项目编号最大50个字符")
    private String projectCode;
    
    @Schema(description = "项目名称", example = "智慧城市项目")
    @Size(min = 2, max = 100, message = "项目名称长度必须在2-100个字符之间")
    private String projectName;
    
    @Schema(description = "项目描述", example = "这是一个智慧城市建设项目的描述")
    private String description;
    
    @Schema(description = "项目图片URL")
    private String imageUrl;
    
    @Schema(description = "状态：1-进行中，2-已完成，0-已暂停", example = "1", allowableValues = {"0", "1", "2"})
    private Integer status;
    
    @Schema(description = "开始日期", example = "2024-01-01T00:00:00")
    private LocalDateTime startDate;
    
    @Schema(description = "结束日期", example = "2024-12-31T23:59:59")
    private LocalDateTime endDate;
}
