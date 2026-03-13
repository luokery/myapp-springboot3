package com.example.usermanagement.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * 项目状态校验注解
 * 有效值：0-已暂停，1-进行中，2-已完成
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = ProjectStatusValidator.class)
public @interface ProjectStatus {

    String message() default "项目状态无效，有效值为：0-已暂停，1-进行中，2-已完成";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /**
     * 是否允许为空
     */
    boolean required() default false;
}
