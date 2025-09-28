package com.pocopi.api.exception;

import com.pocopi.api.dto.api.FieldErrorResponse;
import lombok.Getter;

import java.util.List;

@Getter
public class MultiFieldException extends RuntimeException {

    private final List<FieldErrorResponse> errors;

    public MultiFieldException(String message, List<FieldErrorResponse> errors) {
        super(message);
        this.errors = errors;
    }

}