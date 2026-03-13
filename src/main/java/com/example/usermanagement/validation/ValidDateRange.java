package com.example.usermanagement.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * 日期范围校验注解
 * 用于校验开始日期必须早于结束日期
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = DateRangeValidator.class)
public @interface ValidDateRange {

    String message() default "开始日期必须早于结束日期";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /**
     * 开始日期字段名
     */
    String startField() default "startDate";

    /**
     * 结束日期字段名
     */
    String endField() default "endDate";

    /**
     * 是否允许为空（当任一日期为空时跳过校验）
     */
    boolean allowEmpty() default true;
}
