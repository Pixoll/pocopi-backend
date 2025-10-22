package com.pocopi.api.exception;

import com.pocopi.api.dto.api.ApiHttpError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MultiFieldException.class)
    public ResponseEntity<ApiHttpError> multiFieldException(MultiFieldException e) {
        final ApiHttpError body = new ApiHttpError(
            HttpStatus.BAD_REQUEST.value(),
            e.getMessage(),
            e.getErrors()
        );

        return ResponseEntity.badRequest().body(body);
    }
}
