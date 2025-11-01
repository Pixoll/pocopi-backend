package com.pocopi.api.dto.api;

import com.pocopi.api.exception.HttpException;
import com.pocopi.api.exception.MultiFieldException;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.HttpStatus;

import java.util.List;

public record ApiHttpError(
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    int code,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String message,

    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED, nullable = true)
    List<FieldError> errors
) {
    public ApiHttpError(HttpStatus code, String message, List<FieldError> errors) {
        this(code.value(), message, errors);
    }

    public ApiHttpError(HttpStatus code, String message) {
        this(code.value(), message, null);
    }

    public ApiHttpError(HttpException exception) {
        this(exception.getStatus(), exception.getMessage());
    }

    public ApiHttpError(MultiFieldException exception) {
        this(HttpStatus.UNPROCESSABLE_ENTITY, exception.getMessage(), exception.getErrors());
    }

    public ApiHttpError(Exception exception) {
        this(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
    }
}
