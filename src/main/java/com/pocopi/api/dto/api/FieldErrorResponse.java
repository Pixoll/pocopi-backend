package com.pocopi.api.dto.api;

public record FieldErrorResponse(
        String field,
        String message
) {
}
