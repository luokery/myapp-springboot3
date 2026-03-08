package com.example.usermanagement.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Project {
    private Long id;
    private String projectCode;      // 项目编号
    private String projectName;      // 项目名称
    private String description;      // 项目描述
    private String imageUrl;         // 项目图片URL
    private Integer status;          // 状态：1-进行中，2-已完成，0-已暂停
    private LocalDateTime startDate; // 开始日期
    private LocalDateTime endDate;   // 结束日期
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
