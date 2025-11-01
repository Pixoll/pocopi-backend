package com.pocopi.api.exception;

import com.pocopi.api.dto.api.ApiHttpError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(HttpException.class)
    public ResponseEntity<ApiHttpError> httpException(HttpException exception) {
        return new ResponseEntity<>(new ApiHttpError(exception), exception.getStatus());
    }

    @ExceptionHandler(MultiFieldException.class)
    public ResponseEntity<ApiHttpError> multiFieldException(MultiFieldException exception) {
        return ResponseEntity.unprocessableEntity().body(new ApiHttpError(exception));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiHttpError> genericException(Exception exception) {
        LOGGER.error(exception.getMessage(), exception);
        return ResponseEntity.internalServerError().body(new ApiHttpError(exception));
    }
}
