package com.example.usermanagement.controller;

import com.example.usermanagement.dto.FileUploadResponseDTO;
import com.example.usermanagement.entity.FileAccessType;
import com.example.usermanagement.entity.FileInfo;
import com.example.usermanagement.service.FileService;
import com.example.usermanagement.shiro.JwtToken;
import com.example.usermanagement.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 文件上传控制器
 * 支持公开文件和私密文件的上传与访问
 */
@Slf4j
@Tag(name = "文件管理", description = "文件上传和下载接口")
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class FileController {

    private final FileService fileService;
    private final JwtUtil jwtUtil;

    /**
     * 通用文件上传接口
     * 
     * @param file         文件
     * @param accessType   访问类型：public（公开，不需要认证）或 private（私密，需要认证）
     * @param businessType 业务类型（可选，如：avatar, document, project-image等）
     * @param businessId   业务ID（可选）
     * @param request      HTTP请求（用于获取当前用户信息）
     * @return 文件上传响应
     */
    @Operation(summary = "上传文件", description = "上传文件，支持公开和私密两种访问类型")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "文件上传成功"),
            @ApiResponse(responseCode = "400", description = "参数错误"),
            @ApiResponse(responseCode = "401", description = "未认证（私密文件需要认证）")
    })
    @PostMapping("/upload")
    public ResponseEntity<Result<FileUploadResponseDTO>> uploadFile(
            @Parameter(description = "文件", required = true) @RequestParam("file") MultipartFile file,
            @Parameter(description = "访问类型：public（公开）或 private（私密）") 
            @RequestParam(value = "accessType", defaultValue = "public") String accessType,
            @Parameter(description = "业务类型（如：avatar, document, project-image）") 
            @RequestParam(value = "businessType", required = false) String businessType,
            @Parameter(description = "业务ID") 
            @RequestParam(value = "businessId", required = false) Long businessId,
            HttpServletRequest request) {

        FileAccessType type = FileAccessType.fromCode(accessType);
        
        // 私密文件需要认证
        Long uploaderId = null;
        if (type == FileAccessType.PRIVATE) {
            uploaderId = getCurrentUserId(request);
            if (uploaderId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Result.error(401, "私密文件上传需要认证"));
            }
        }

        try {
            FileUploadResponseDTO response = fileService.uploadFile(file, type, businessType, businessId, uploaderId);
            return ResponseEntity.ok(Result.success(response, "文件上传成功"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Result.error(400, e.getMessage()));
        } catch (IOException e) {
            log.error("文件上传失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error(500, "文件上传失败: " + e.getMessage()));
        }
    }

    /**
     * 上传图片（公开）
     * 便捷接口，上传公开访问的图片
     */
    @Operation(summary = "上传公开图片", description = "上传公开访问的图片，不需要认证")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "图片上传成功"),
            @ApiResponse(responseCode = "400", description = "参数错误")
    })
    @PostMapping("/upload/image/public")
    public ResponseEntity<Result<FileUploadResponseDTO>> uploadPublicImage(
            @Parameter(description = "图片文件", required = true) @RequestParam("file") MultipartFile file,
            @Parameter(description = "业务类型") @RequestParam(value = "businessType", required = false) String businessType,
            @Parameter(description = "业务ID") @RequestParam(value = "businessId", required = false) Long businessId) {

        // 验证文件类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return ResponseEntity.badRequest()
                    .body(Result.error(400, "只能上传图片文件"));
        }

        try {
            FileUploadResponseDTO response = fileService.uploadFile(
                    file, FileAccessType.PUBLIC, businessType, businessId, null);
            return ResponseEntity.ok(Result.success(response, "图片上传成功"));
        } catch (IOException e) {
            log.error("图片上传失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error(500, "图片上传失败: " + e.getMessage()));
        }
    }

    /**
     * 上传图片（私密）
     * 需要认证
     */
    @Operation(summary = "上传私密图片", description = "上传需要认证才能访问的图片")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "图片上传成功"),
            @ApiResponse(responseCode = "400", description = "参数错误"),
            @ApiResponse(responseCode = "401", description = "未认证")
    })
    @PostMapping("/upload/image/private")
    public ResponseEntity<Result<FileUploadResponseDTO>> uploadPrivateImage(
            @Parameter(description = "图片文件", required = true) @RequestParam("file") MultipartFile file,
            @Parameter(description = "业务类型") @RequestParam(value = "businessType", required = false) String businessType,
            @Parameter(description = "业务ID") @RequestParam(value = "businessId", required = false) Long businessId,
            HttpServletRequest request) {

        Long uploaderId = getCurrentUserId(request);
        if (uploaderId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Result.error(401, "私密图片上传需要认证"));
        }

        // 验证文件类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return ResponseEntity.badRequest()
                    .body(Result.error(400, "只能上传图片文件"));
        }

        try {
            FileUploadResponseDTO response = fileService.uploadFile(
                    file, FileAccessType.PRIVATE, businessType, businessId, uploaderId);
            return ResponseEntity.ok(Result.success(response, "私密图片上传成功"));
        } catch (IOException e) {
            log.error("私密图片上传失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error(500, "图片上传失败: " + e.getMessage()));
        }
    }

    /**
     * 上传文档（私密）
     * 文档类型文件默认为私密访问
     */
    @Operation(summary = "上传私密文档", description = "上传需要认证才能访问的文档")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "文档上传成功"),
            @ApiResponse(responseCode = "400", description = "参数错误"),
            @ApiResponse(responseCode = "401", description = "未认证")
    })
    @PostMapping("/upload/document")
    public ResponseEntity<Result<FileUploadResponseDTO>> uploadDocument(
            @Parameter(description = "文档文件", required = true) @RequestParam("file") MultipartFile file,
            @Parameter(description = "业务类型") @RequestParam(value = "businessType", required = false) String businessType,
            @Parameter(description = "业务ID") @RequestParam(value = "businessId", required = false) Long businessId,
            HttpServletRequest request) {

        Long uploaderId = getCurrentUserId(request);
        if (uploaderId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Result.error(401, "文档上传需要认证"));
        }

        // 验证文件类型（允许常见文档类型）
        String contentType = file.getContentType();
        if (contentType == null || !isAllowedDocumentType(contentType)) {
            return ResponseEntity.badRequest()
                    .body(Result.error(400, "不支持的文档类型，支持：PDF、Word、Excel、PowerPoint、TXT"));
        }

        try {
            FileUploadResponseDTO response = fileService.uploadFile(
                    file, FileAccessType.PRIVATE, businessType, businessId, uploaderId);
            return ResponseEntity.ok(Result.success(response, "文档上传成功"));
        } catch (IOException e) {
            log.error("文档上传失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error(500, "文档上传失败: " + e.getMessage()));
        }
    }

    /**
     * 下载私密文件
     * 需要认证
     */
    @Operation(summary = "下载私密文件", description = "下载私密文件，需要认证")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "文件下载成功"),
            @ApiResponse(responseCode = "401", description = "未认证"),
            @ApiResponse(responseCode = "404", description = "文件不存在")
    })
    @GetMapping("/download/{filename}")
    public ResponseEntity<Resource> downloadPrivateFile(
            @Parameter(description = "文件名", required = true) @PathVariable String filename,
            HttpServletRequest request) {

        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        FileInfo fileInfo = fileService.getFileInfoByStoredName(filename, FileAccessType.PRIVATE);
        if (fileInfo == null) {
            return ResponseEntity.notFound().build();
        }

        try {
            byte[] content = fileService.getFileContent(fileInfo);
            ByteArrayResource resource = new ByteArrayResource(content);

            String encodedFilename = URLEncoder.encode(fileInfo.getOriginalFilename(), StandardCharsets.UTF_8)
                    .replaceAll("\\+", "%20");

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(fileInfo.getContentType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                            "attachment; filename*=UTF-8''" + encodedFilename)
                    .body(resource);
        } catch (IOException e) {
            log.error("文件下载失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 获取文件信息
     */
    @Operation(summary = "获取文件信息", description = "根据文件ID获取文件信息")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取成功"),
            @ApiResponse(responseCode = "404", description = "文件不存在")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Result<FileInfo>> getFileInfo(
            @Parameter(description = "文件ID", required = true) @PathVariable Long id) {
        FileInfo fileInfo = fileService.getFileInfo(id);
        if (fileInfo == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Result.error(404, "文件不存在"));
        }
        return ResponseEntity.ok(Result.success(fileInfo, "获取文件信息成功"));
    }

    /**
     * 删除文件
     */
    @Operation(summary = "删除文件", description = "删除文件（仅限上传者或管理员）")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "删除成功"),
            @ApiResponse(responseCode = "401", description = "未认证"),
            @ApiResponse(responseCode = "403", description = "无权限"),
            @ApiResponse(responseCode = "404", description = "文件不存在")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Result<Void>> deleteFile(
            @Parameter(description = "文件ID", required = true) @PathVariable Long id,
            HttpServletRequest request) {

        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Result.error(401, "删除文件需要认证"));
        }

        FileInfo fileInfo = fileService.getFileInfo(id);
        if (fileInfo == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Result.error(404, "文件不存在"));
        }

        // 检查权限（上传者可以删除）
        if (fileInfo.getUploaderId() != null && !fileInfo.getUploaderId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Result.error(403, "无权删除此文件"));
        }

        boolean deleted = fileService.deleteFile(id);
        if (deleted) {
            return ResponseEntity.ok(Result.success(null, "文件删除成功"));
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error(500, "文件删除失败"));
        }
    }

    /**
     * 从请求中获取当前用户ID
     */
    private Long getCurrentUserId(HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        if (token == null) {
            return null;
        }
        try {
            return jwtUtil.getUserIdFromToken(token);
        } catch (Exception e) {
            log.warn("解析token失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从请求中获取Token
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String header = request.getHeader(jwtUtil.getHeader());
        if (header != null && header.startsWith(jwtUtil.getPrefix())) {
            return header.substring(jwtUtil.getPrefix().length());
        }
        return null;
    }

    /**
     * 判断是否为允许的文档类型
     */
    private boolean isAllowedDocumentType(String contentType) {
        return contentType != null && (
                contentType.equals("application/pdf") ||
                contentType.contains("word") ||
                contentType.contains("excel") ||
                contentType.contains("spreadsheet") ||
                contentType.contains("powerpoint") ||
                contentType.contains("presentation") ||
                contentType.contains("msword") ||
                contentType.equals("text/plain") ||
                contentType.contains("officedocument")
        );
    }
}
