package com.example.usermanagement.entity;

import lombok.Getter;

/**
 * 文件访问级别枚举
 */
@Getter
public enum FileAccessType {
    /**
     * 公开访问 - 不需要认证
     */
    PUBLIC("public", "公开文件", "/uploads/public/"),
    
    /**
     * 私密访问 - 需要认证
     */
    PRIVATE("private", "私密文件", "/uploads/private/");

    private final String code;
    private final String description;
    private final String urlPrefix;

    FileAccessType(String code, String description, String urlPrefix) {
        this.code = code;
        this.description = description;
        this.urlPrefix = urlPrefix;
    }

    /**
     * 根据代码获取访问类型
     */
    public static FileAccessType fromCode(String code) {
        if (code == null) {
            return PUBLIC; // 默认公开
        }
        for (FileAccessType type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        return PUBLIC;
    }
}
