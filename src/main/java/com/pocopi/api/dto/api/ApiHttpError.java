package com.pocopi.api.dto.api;

import com.pocopi.api.exception.HttpException;
import com.pocopi.api.exception.MultiFieldException;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

public record ApiHttpError(
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    int code,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String message,

    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED, nullable = true)
    List<FieldError> errors
) {
    public ApiHttpError(HttpException exception) {
        this(exception.getStatus().value(), exception.getMessage(), null);
    }

    public ApiHttpError(MultiFieldException exception) {
        this(HttpStatus.UNPROCESSABLE_ENTITY.value(), exception.getMessage(), exception.getErrors());
    }

    public ApiHttpError(HttpStatus code, Exception exception) {
        this(code.value(), exception.getMessage(), null);
    }

    public ResponseEntity<ApiHttpError> toResponseEntity() {
        return ResponseEntity.status(code).body(this);
    }
}
