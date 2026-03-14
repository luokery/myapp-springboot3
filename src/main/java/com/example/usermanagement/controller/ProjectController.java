package com.example.usermanagement.controller;

import com.example.usermanagement.dto.PageDTO;
import com.example.usermanagement.dto.ProjectCreateDTO;
import com.example.usermanagement.dto.ProjectQueryDTO;
import com.example.usermanagement.dto.ProjectResponseDTO;
import com.example.usermanagement.dto.ProjectUpdateDTO;
import com.example.usermanagement.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Tag(name = "项目管理", description = "项目的增删改查和图片上传接口")
@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ProjectController {

    private final ProjectService projectService;

    @Value("${file.project-dir:/tmp/uploads}")
    private String uploadDir;
    @Value("${file.project-url:/uploads/projects}")
    private String uploadUrl;
    
    @Operation(summary = "分页查询项目", description = "支持多条件查询、分页和排序")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "成功获取项目列表"),
            @ApiResponse(responseCode = "400", description = "参数验证失败")
    })
    @GetMapping("/page")
    public ResponseEntity<Result<PageDTO<ProjectResponseDTO>>> queryProjects(
            @Parameter(description = "项目编号（模糊匹配）") @RequestParam(required = false) String projectCode,
            @Parameter(description = "项目名称（模糊匹配）") @RequestParam(required = false) String projectName,
            @Parameter(description = "项目状态") @RequestParam(required = false) Integer status,
            @Parameter(description = "关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "项目开始日期（起始）") @RequestParam(required = false) String startDateFrom,
            @Parameter(description = "项目开始日期（结束）") @RequestParam(required = false) String startDateTo,
            @Parameter(description = "项目结束日期（起始）") @RequestParam(required = false) String endDateFrom,
            @Parameter(description = "项目结束日期（结束）") @RequestParam(required = false) String endDateTo,
            @Parameter(description = "创建时间（起始）") @RequestParam(required = false) String createdAtFrom,
            @Parameter(description = "创建时间（结束）") @RequestParam(required = false) String createdAtTo,
            @Parameter(description = "页码（从0开始）") @RequestParam(defaultValue = "0") int pageNumber,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int pageSize,
            @Parameter(description = "排序字段") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "排序方向") @RequestParam(defaultValue = "DESC") String sortDirection) {

        ProjectQueryDTO query = new ProjectQueryDTO();
        query.setProjectCode(projectCode);
        query.setProjectName(projectName);
        query.setStatus(status);
        query.setKeyword(keyword);
        query.setStartDateFrom(parseDateTime(startDateFrom));
        query.setStartDateTo(parseDateTime(startDateTo));
        query.setEndDateFrom(parseDateTime(endDateFrom));
        query.setEndDateTo(parseDateTime(endDateTo));
        query.setCreatedAtFrom(parseDateTime(createdAtFrom));
        query.setCreatedAtTo(parseDateTime(createdAtTo));
        query.setPageNumber(pageNumber);
        query.setPageSize(pageSize);
        query.setSortBy(sortBy);
        query.setSortDirection(sortDirection);

        PageDTO<ProjectResponseDTO> result = projectService.queryProjects(query);
        return ResponseEntity.ok(Result.success(result, "查询成功"));
    }

    /**
     * 解析日期时间字符串
     */
    private LocalDateTime parseDateTime(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDateTime.parse(dateStr);
        } catch (Exception e) {
            // 尝试只解析日期部分（格式：yyyy-MM-dd）
            try {
                return java.time.LocalDate.parse(dateStr).atStartOfDay();
            } catch (Exception ex) {
                return null;
            }
        }
    }

    @Operation(summary = "获取项目列表", description = "获取所有项目的列表信息")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "成功获取项目列表")
    })
    @GetMapping
    public ResponseEntity<Result<List<ProjectResponseDTO>>> getAllProjects() {
        List<ProjectResponseDTO> projects = projectService.getAllProjects();
        return ResponseEntity.ok(Result.success(projects, "获取项目列表成功"));
    }

    @Operation(summary = "获取项目详情", description = "根据项目ID获取项目详细信息")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "成功获取项目详情"),
            @ApiResponse(responseCode = "400", description = "项目不存在")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Result<ProjectResponseDTO>> getProjectById(
            @Parameter(description = "项目ID", required = true) @PathVariable Long id) {
        ProjectResponseDTO project = projectService.getProjectById(id);
        return ResponseEntity.ok(Result.success(project, "获取项目详情成功"));
    }

    @Operation(summary = "搜索项目", description = "根据关键词搜索项目（支持项目名称和编号模糊匹配）")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "搜索成功"),
            @ApiResponse(responseCode = "400", description = "关键词不能为空")
    })
    @GetMapping("/search")
    public ResponseEntity<Result<List<ProjectResponseDTO>>> searchProjects(
            @Parameter(description = "搜索关键词", required = true) @RequestParam String keyword) {
        List<ProjectResponseDTO> projects = projectService.searchProjects(keyword);
        return ResponseEntity.ok(Result.success(projects, "搜索项目成功"));
    }

    @Operation(summary = "按状态获取项目", description = "根据状态获取项目列表")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "成功获取项目列表"),
            @ApiResponse(responseCode = "400", description = "状态值无效")
    })
    @GetMapping("/status/{status}")
    public ResponseEntity<Result<List<ProjectResponseDTO>>> getProjectsByStatus(
            @Parameter(description = "状态：1-进行中，2-已完成，0-已暂停", required = true) @PathVariable Integer status) {
        List<ProjectResponseDTO> projects = projectService.getProjectsByStatus(status);
        return ResponseEntity.ok(Result.success(projects, "获取项目列表成功"));
    }

    @Operation(summary = "创建项目", description = "创建新项目")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "项目创建成功"),
            @ApiResponse(responseCode = "400", description = "参数验证失败或项目编号已存在")
    })
    @PostMapping
    public ResponseEntity<Result<ProjectResponseDTO>> createProject(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "项目创建信息", required = true)
            @Valid @RequestBody ProjectCreateDTO dto) {
        ProjectResponseDTO project = projectService.createProject(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Result.success(project, "创建项目成功"));
    }

    @Operation(summary = "更新项目", description = "根据项目ID更新项目信息")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "项目更新成功"),
            @ApiResponse(responseCode = "400", description = "项目不存在或参数验证失败")
    })
    @PutMapping("/{id}")
    public ResponseEntity<Result<ProjectResponseDTO>> updateProject(
            @Parameter(description = "项目ID", required = true) @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "项目更新信息", required = true)
            @Valid @RequestBody ProjectUpdateDTO dto) {
        ProjectResponseDTO project = projectService.updateProject(id, dto);
        return ResponseEntity.ok(Result.success(project, "更新项目成功"));
    }

    @Operation(summary = "上传项目图片", description = "为项目上传图片")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "图片上传成功"),
            @ApiResponse(responseCode = "400", description = "项目不存在或文件上传失败")
    })
    @PostMapping("/{id}/image")
    public ResponseEntity<Result<Map<String, String>>> uploadProjectImage(
            @Parameter(description = "项目ID", required = true) @PathVariable Long id,
            @Parameter(description = "项目图片文件", required = true) @RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Result.error(400, "请选择要上传的文件"));
        }

        try {
            // 检查文件类型
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest()
                        .body(Result.error(400, "只能上传图片文件"));
            }

            // 创建上传目录
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".")
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : ".jpg";
            String newFilename = UUID.randomUUID().toString() + extension;

            // 保存文件
            Path filePath = uploadPath.resolve(newFilename);
//            file.transferTo(filePath.toFile());

            file.transferTo(new File(filePath.toUri()));
            
            // 生成访问URL
            String imageUrl = MessageFormat.format( "{0}/{1}", uploadUrl, newFilename);

            // 更新项目图片URL
            projectService.updateProjectImage(id, imageUrl);

            Map<String, String> data = new HashMap<>();
            data.put("imageUrl", imageUrl);
            data.put("filename", newFilename);

            return ResponseEntity.ok(Result.success(data, "图片上传成功"));

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error(500, "文件上传失败: " + e.getMessage()));
        }
    }

    @Operation(summary = "删除项目", description = "根据项目ID删除项目")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "项目删除成功"),
            @ApiResponse(responseCode = "400", description = "项目不存在")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Result<Void>> deleteProject(
            @Parameter(description = "项目ID", required = true) @PathVariable Long id) {
        projectService.deleteProject(id);
        return ResponseEntity.ok(Result.success(null, "删除项目成功"));
    }

    @Operation(summary = "获取项目数量", description = "获取系统中项目的总数")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "成功获取项目数量")
    })
    @GetMapping("/count")
    public ResponseEntity<Result<Map<String, Object>>> getProjectCount() {
        Map<String, Object> data = new HashMap<>();
        data.put("total", projectService.getProjectCount());
        data.put("inProgress", projectService.getProjectCountByStatus(1));
        data.put("completed", projectService.getProjectCountByStatus(2));
        data.put("paused", projectService.getProjectCountByStatus(0));
        return ResponseEntity.ok(Result.success(data, "获取项目数量成功"));
    }
}
