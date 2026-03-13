package com.example.usermanagement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springdoc.core.annotations.ParameterObject;

import java.time.LocalDateTime;

/**
 * 项目查询参数 DTO
 */
@Data
@ParameterObject
@Schema(description = "项目查询参数")
public class ProjectQueryDTO {

    @Schema(description = "项目编号（模糊匹配）", example = "PRJ-2024")
    private String projectCode;

    @Schema(description = "项目名称（模糊匹配）", example = "智慧")
    private String projectName;

    @Schema(description = "项目状态：0-已暂停，1-进行中，2-已完成", example = "1", allowableValues = {"0", "1", "2"})
    private Integer status;

    @Schema(description = "开始日期（起始范围）", example = "2024-01-01T00:00:00")
    private LocalDateTime startDateFrom;

    @Schema(description = "开始日期（结束范围）", example = "2024-12-31T23:59:59")
    private LocalDateTime startDateTo;

    @Schema(description = "结束日期（起始范围）", example = "2024-01-01T00:00:00")
    private LocalDateTime endDateFrom;

    @Schema(description = "结束日期（结束范围）", example = "2024-12-31T23:59:59")
    private LocalDateTime endDateTo;

    @Schema(description = "创建时间（起始范围）", example = "2024-01-01T00:00:00")
    private LocalDateTime createdAtFrom;

    @Schema(description = "创建时间（结束范围）", example = "2024-12-31T23:59:59")
    private LocalDateTime createdAtTo;

    @Schema(description = "关键词（同时匹配编号和名称）", example = "智慧城市")
    private String keyword;

    @Schema(description = "页码（从0开始）", example = "0", defaultValue = "0")
    private int pageNumber = 0;

    @Schema(description = "每页大小", example = "10", defaultValue = "10")
    private int pageSize = 10;

    @Schema(description = "排序字段", example = "createdAt", allowableValues = {"id", "projectCode", "projectName", "status", "startDate", "endDate", "createdAt", "updatedAt"})
    private String sortBy = "createdAt";

    @Schema(description = "排序方向", example = "DESC", allowableValues = {"ASC", "DESC"})
    private String sortDirection = "DESC";

    /**
     * 获取OFFSET值（用于分页查询）
     */
    public int getOffset() {
        return pageNumber * pageSize;
    }

    /**
     * 获取排序方向（默认 DESC）
     */
    public String getSortDirection() {
        if ("ASC".equalsIgnoreCase(sortDirection)) {
            return "ASC";
        }
        return "DESC";
    }

    /**
     * 获取排序字段（转换为数据库字段名）
     */
    public String getSortBy() {
        if (sortBy == null || sortBy.isEmpty()) {
            return "created_at";
        }
        // 转换 camelCase 到 snake_case
        return sortBy.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }

    /**
     * 验证日期范围是否有效
     */
    public boolean isValidDateRange() {
        // 验证开始日期范围
        if (startDateFrom != null && startDateTo != null && startDateFrom.isAfter(startDateTo)) {
            return false;
        }
        // 验证结束日期范围
        if (endDateFrom != null && endDateTo != null && endDateFrom.isAfter(endDateTo)) {
            return false;
        }
        // 验证创建时间范围
        if (createdAtFrom != null && createdAtTo != null && createdAtFrom.isAfter(createdAtTo)) {
            return false;
        }
        return true;
    }

    /**
     * 验证分页参数是否有效
     */
    public boolean isValidPagination() {
        return pageNumber >= 0 && pageSize > 0 && pageSize <= 100;
    }
}
