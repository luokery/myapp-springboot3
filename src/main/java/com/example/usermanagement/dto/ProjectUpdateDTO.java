package com.example.usermanagement.dto;

import com.example.usermanagement.validation.ProjectCode;
import com.example.usermanagement.validation.ProjectStatus;
import com.example.usermanagement.validation.ValidDateRange;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 项目更新请求 DTO
 */
@Data
@Schema(description = "项目更新请求")
@ValidDateRange(startField = "startDate", endField = "endDate", message = "开始日期必须早于结束日期")
public class ProjectUpdateDTO {

    @ProjectCode(required = false, message = "项目编号格式不正确，应为 PRJ-YYYY-NNN 格式（如 PRJ-2024-001）")
    @Schema(description = "项目编号（格式：PRJ-YYYY-NNN，如 PRJ-2024-001）", example = "PRJ-2024-001")
    private String projectCode;

    @Size(min = 2, max = 100, message = "项目名称长度必须在2-100个字符之间")
    @Schema(description = "项目名称", example = "智慧城市项目")
    private String projectName;

    @Size(max = 2000, message = "项目描述最大2000个字符")
    @Schema(description = "项目描述", example = "这是一个智慧城市建设项目的描述")
    private String description;

    @Schema(description = "项目图片URL")
    @Size(max = 500, message = "图片URL最大500个字符")
    private String imageUrl;

    @ProjectStatus(message = "项目状态无效，有效值为：0-已暂停，1-进行中，2-已完成")
    @Schema(description = "状态：0-已暂停，1-进行中，2-已完成", example = "1", allowableValues = {"0", "1", "2"})
    private Integer status;

    @Schema(description = "开始日期", example = "2024-01-01T00:00:00")
    private LocalDateTime startDate;

    @Schema(description = "结束日期", example = "2024-12-31T23:59:59")
    private LocalDateTime endDate;
    
    @Schema(description = "乐观锁版本号（必须与当前数据库中的版本一致）", example = "1", required = true)
    private Integer version;
}
