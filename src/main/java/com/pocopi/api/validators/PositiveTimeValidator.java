package com.pocopi.api.validators;

import com.pocopi.api.annotations.PositiveTime;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalTime;

public class PositiveTimeValidator implements ConstraintValidator<PositiveTime, LocalTime> {
    @Override
    public boolean isValid(LocalTime value, ConstraintValidatorContext ctx) {
        return value == null || value.isAfter(LocalTime.MIDNIGHT);
    }
}
