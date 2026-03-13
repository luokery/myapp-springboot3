package com.example.usermanagement.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;

import java.time.LocalDateTime;
import java.time.LocalDate;

/**
 * 日期范围校验器
 */
public class DateRangeValidator implements ConstraintValidator<ValidDateRange, Object> {

    private String startField;
    private String endField;
    private boolean allowEmpty;

    @Override
    public void initialize(ValidDateRange constraintAnnotation) {
        this.startField = constraintAnnotation.startField();
        this.endField = constraintAnnotation.endField();
        this.allowEmpty = constraintAnnotation.allowEmpty();
    }

    @Override
    public boolean isValid(Object obj, ConstraintValidatorContext context) {
        if (obj == null) {
            return true;
        }

        try {
            BeanWrapper beanWrapper = PropertyAccessorFactory.forBeanPropertyAccess(obj);
            Object startValue = beanWrapper.getPropertyValue(startField);
            Object endValue = beanWrapper.getPropertyValue(endField);

            // 如果允许为空且任一为空，则跳过校验
            if (allowEmpty && (startValue == null || endValue == null)) {
                return true;
            }

            // 比较日期
            if (startValue != null && endValue != null) {
                int comparison = compareDates(startValue, endValue);
                if (comparison > 0) {
                    context.disableDefaultConstraintViolation();
                    context.buildConstraintViolationWithTemplate(startField + " 必须早于或等于 " + endField)
                            .addPropertyNode(startField)
                            .addConstraintViolation();
                    return false;
                }
            }

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 比较两个日期对象
     */
    @SuppressWarnings("unchecked")
    private int compareDates(Object start, Object end) {
        if (start instanceof LocalDateTime && end instanceof LocalDateTime) {
            return ((LocalDateTime) start).compareTo((LocalDateTime) end);
        } else if (start instanceof LocalDate && end instanceof LocalDate) {
            return ((LocalDate) start).compareTo((LocalDate) end);
        } else if (start instanceof Comparable && end instanceof Comparable) {
            return ((Comparable<Object>) start).compareTo(end);
        }
        throw new IllegalArgumentException("不支持日期类型: " + start.getClass().getName());
    }
}
