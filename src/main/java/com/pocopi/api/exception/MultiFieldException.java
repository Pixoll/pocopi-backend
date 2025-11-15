package com.pocopi.api.exception;

import com.pocopi.api.dto.api.FieldError;
import lombok.Getter;

import java.util.List;

@Getter
public class MultiFieldException extends ApiException {
    private final List<FieldError> errors;

    public MultiFieldException(String message, List<FieldError> errors) {
        super(message);
        this.errors = errors;
    }
}
