package com.example.usermanagement.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * 项目编号格式校验注解
 * 格式要求：PRJ-YYYY-NNN
 * - PRJ-：固定前缀
 * - YYYY：4位年份
 * - NNN：3位序号
 * 示例：PRJ-2024-001, PRJ-2024-123
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = ProjectCodeValidator.class)
public @interface ProjectCode {

    String message() default "项目编号格式不正确，应为 PRJ-YYYY-NNN 格式（如 PRJ-2024-001）";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /**
     * 是否允许为空
     */
    boolean required() default true;
}
