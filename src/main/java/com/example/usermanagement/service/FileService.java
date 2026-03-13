package com.example.usermanagement.service;

import com.example.usermanagement.dto.FileUploadResponseDTO;
import com.example.usermanagement.entity.FileAccessType;
import com.example.usermanagement.entity.FileInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 文件上传服务
 */
@Slf4j
@Service
public class FileService {

    @Value("${file.upload-dir:/tmp/uploads}")
    private String baseUploadDir;

    // 模拟文件存储（实际项目中应该使用数据库）
    private final Map<Long, FileInfo> fileStore = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    /**
     * 上传文件
     *
     * @param file         文件
     * @param accessType   访问类型
     * @param businessType 业务类型
     * @param businessId   业务ID
     * @param uploaderId   上传者ID
     * @return 文件上传响应
     */
    public FileUploadResponseDTO uploadFile(MultipartFile file, FileAccessType accessType,
                                            String businessType, Long businessId, Long uploaderId) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }

        // 获取文件信息
        String originalFilename = file.getOriginalFilename();
        String contentType = file.getContentType();
        long fileSize = file.getSize();
        String extension = getFileExtension(originalFilename);

        // 生成存储文件名
        String storedFilename = UUID.randomUUID().toString() + extension;

        // 确定存储路径
        String subDir = accessType.getCode();
        Path uploadPath = Paths.get(baseUploadDir, subDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // 保存文件
        Path filePath = uploadPath.resolve(storedFilename);
        file.transferTo(filePath.toFile());

        // 生成访问URL
        String accessUrl = accessType.getUrlPrefix() + storedFilename;

        // 保存文件信息
        FileInfo fileInfo = FileInfo.builder()
                .id(idGenerator.getAndIncrement())
                .originalFilename(originalFilename)
                .storedFilename(storedFilename)
                .filePath(filePath.toString())
                .accessUrl(accessUrl)
                .fileSize(fileSize)
                .contentType(contentType)
                .extension(extension)
                .accessType(accessType)
                .businessType(businessType)
                .businessId(businessId)
                .uploaderId(uploaderId)
                .createdAt(LocalDateTime.now())
                .deleted(false)
                .build();

        fileStore.put(fileInfo.getId(), fileInfo);

        log.info("文件上传成功: id={}, accessType={}, originalFilename={}, accessUrl={}",
                fileInfo.getId(), accessType, originalFilename, accessUrl);

        return FileUploadResponseDTO.builder()
                .id(fileInfo.getId())
                .originalFilename(originalFilename)
                .storedFilename(storedFilename)
                .accessUrl(accessUrl)
                .fileSize(fileSize)
                .contentType(contentType)
                .accessType(accessType.getCode())
                .businessType(businessType)
                .build();
    }

    /**
     * 获取文件信息
     */
    public FileInfo getFileInfo(Long fileId) {
        FileInfo fileInfo = fileStore.get(fileId);
        if (fileInfo == null || Boolean.TRUE.equals(fileInfo.getDeleted())) {
            return null;
        }
        return fileInfo;
    }

    /**
     * 根据存储文件名获取文件信息
     */
    public FileInfo getFileInfoByStoredName(String storedFilename, FileAccessType accessType) {
        return fileStore.values().stream()
                .filter(f -> f.getStoredFilename().equals(storedFilename) 
                        && f.getAccessType() == accessType
                        && !Boolean.TRUE.equals(f.getDeleted()))
                .findFirst()
                .orElse(null);
    }

    /**
     * 获取文件内容
     */
    public byte[] getFileContent(FileInfo fileInfo) throws IOException {
        Path filePath = Paths.get(fileInfo.getFilePath());
        if (!Files.exists(filePath)) {
            throw new IOException("文件不存在: " + fileInfo.getStoredFilename());
        }
        return Files.readAllBytes(filePath);
    }

    /**
     * 删除文件
     */
    public boolean deleteFile(Long fileId) {
        FileInfo fileInfo = fileStore.get(fileId);
        if (fileInfo == null) {
            return false;
        }

        // 标记为已删除
        fileInfo.setDeleted(true);

        // 删除物理文件
        try {
            Path filePath = Paths.get(fileInfo.getFilePath());
            Files.deleteIfExists(filePath);
            log.info("文件已删除: id={}, filename={}", fileId, fileInfo.getStoredFilename());
        } catch (IOException e) {
            log.warn("删除物理文件失败: {}", e.getMessage());
        }

        return true;
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }

    /**
     * 获取所有文件信息（用于调试）
     */
    public Map<Long, FileInfo> getAllFiles() {
        return new HashMap<>(fileStore);
    }
}
