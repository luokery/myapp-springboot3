package com.example.usermanagement.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;
import java.util.List;

/**
 * 项目状态校验器
 */
public class ProjectStatusValidator implements ConstraintValidator<ProjectStatus, Integer> {

    /**
     * 有效状态值：0-已暂停，1-进行中，2-已完成
     */
    private static final List<Integer> VALID_STATUSES = Arrays.asList(0, 1, 2);

    private boolean required;

    @Override
    public void initialize(ProjectStatus constraintAnnotation) {
        this.required = constraintAnnotation.required();
    }

    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {
        // 如果不是必填且值为空，则通过校验
        if (!required && value == null) {
            return true;
        }

        // 如果是必填且值为空，则不通过
        if (required && value == null) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("项目状态不能为空").addConstraintViolation();
            return false;
        }

        // 校验状态值
        if (value != null && !VALID_STATUSES.contains(value)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("项目状态无效，有效值为：0-已暂停，1-进行中，2-已完成").addConstraintViolation();
            return false;
        }

        return true;
    }
}
