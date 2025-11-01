package com.pocopi.api.exception;

import com.pocopi.api.dto.api.ApiHttpError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(HttpException.class)
    public ResponseEntity<ApiHttpError> httpException(HttpException exception) {
        return new ApiHttpError(exception).toResponseEntity();
    }

    @ExceptionHandler(MultiFieldException.class)
    public ResponseEntity<ApiHttpError> multiFieldException(MultiFieldException exception) {
        return new ApiHttpError(exception).toResponseEntity();
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiHttpError> badCredentialsException(BadCredentialsException exception) {
        return new ApiHttpError(HttpStatus.UNAUTHORIZED, exception).toResponseEntity();
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<ApiHttpError> authorizationDeniedException(AuthorizationDeniedException exception) {
        return new ApiHttpError(HttpStatus.FORBIDDEN, exception).toResponseEntity();
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiHttpError> genericException(Exception exception) {
        LOGGER.error(exception.getMessage(), exception);
        return new ApiHttpError(HttpStatus.INTERNAL_SERVER_ERROR, exception).toResponseEntity();
    }
}
