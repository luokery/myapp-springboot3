package com.example.usermanagement.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

/**
 * 项目编号校验器
 */
public class ProjectCodeValidator implements ConstraintValidator<ProjectCode, String> {

    /**
     * 项目编号正则表达式
     * 格式：PRJ-YYYY-NNN
     * - PRJ-：固定前缀
     * - YYYY：4位年份（1900-2999）
     * - NNN：3位序号（001-999）
     */
    private static final Pattern PROJECT_CODE_PATTERN = Pattern.compile("^PRJ-(19|20)\\d{2}-(00[1-9]|0[1-9]\\d|1\\d{2}|[1-9]\\d{2}|[1-9]\\d)$");

    private boolean required;

    @Override
    public void initialize(ProjectCode constraintAnnotation) {
        this.required = constraintAnnotation.required();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // 如果不是必填且值为空，则通过校验
        if (!required && (value == null || value.trim().isEmpty())) {
            return true;
        }

        // 如果是必填且值为空，则不通过
        if (value == null || value.trim().isEmpty()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("项目编号不能为空").addConstraintViolation();
            return false;
        }

        // 校验格式
        if (!PROJECT_CODE_PATTERN.matcher(value.trim()).matches()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("项目编号格式不正确，应为 PRJ-YYYY-NNN 格式（如 PRJ-2024-001）").addConstraintViolation();
            return false;
        }

        return true;
    }
}
