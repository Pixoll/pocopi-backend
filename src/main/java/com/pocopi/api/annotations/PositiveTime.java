package com.pocopi.api.annotations;

import com.pocopi.api.validators.PositiveTimeValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = PositiveTimeValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface PositiveTime {
    String message() default "timer must be greater than 00:00:00";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
