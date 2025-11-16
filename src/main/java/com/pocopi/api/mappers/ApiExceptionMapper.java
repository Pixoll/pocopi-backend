package com.pocopi.api.mappers;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.exc.InputCoercionException;
import com.fasterxml.jackson.core.json.ReaderBasedJsonParser;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.InvalidTypeIdException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.pocopi.api.dto.api.FieldError;
import com.pocopi.api.dto.config.ConfigUpdate;
import com.pocopi.api.exception.ApiException;
import com.pocopi.api.exception.HttpException;
import com.pocopi.api.exception.MultiFieldException;
import jakarta.validation.ConstraintViolation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.validation.ObjectError;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Component
public class ApiExceptionMapper {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApiExceptionMapper.class);

    public ApiException fromJsonProcessingException(JsonProcessingException e) {
        final String path = parsePath(e instanceof JsonMappingException ex
            ? ex.getPath()
            : List.of()
        );
        final JsonProcessingException exception = e instanceof JsonMappingException ex && ex.getCause() != null
            ? ex.getCause() instanceof JsonProcessingException exc ? exc : e
            : e;

        switch (exception) {
            case UnrecognizedPropertyException ignored -> {
                return new MultiFieldException(
                    "UnrecognizedPropertyException",
                    List.of(new FieldError(path, "Unknown property"))
                );
            }

            case InvalidTypeIdException ex -> {
                return new MultiFieldException(
                    "InvalidTypeIdException",
                    List.of(new FieldError(
                        path,
                        ex.getMessage().split("\n")[0]
                            .replaceFirst("`(?:\\w+\\.)*(\\w+)`", "$1")
                            .replaceFirst(" \\(for POJO property [^)]+\\)$", "")
                    ))
                );
            }

            case InvalidFormatException ex -> {
                if (ex.getTargetType().isEnum()) {
                    return new MultiFieldException(
                        "InvalidFormatException",
                        List.of(new FieldError(
                            path,
                            "Expected one of " + Arrays.toString(ex.getTargetType().getEnumConstants())
                                + ", got " + ex.getValue()
                        ))
                    );
                }

                return new MultiFieldException(
                    "InvalidFormatException",
                    List.of(new FieldError(
                        path,
                        "Expected " + ex.getTargetType().getSimpleName()
                            + ", got " + ex.getValue().getClass().getSimpleName()
                    ))
                );
            }

            case MismatchedInputException ex -> {
                final String token = ex.getProcessor() instanceof ReaderBasedJsonParser processor
                    && processor.currentToken() != null
                    ? processor.currentToken().asString()
                    : null;

                if (token != null) {
                    return new MultiFieldException(
                        "MismatchedInputException",
                        List.of(new FieldError(
                            path,
                            "Expected " + ex.getTargetType().getSimpleName() + ", got " + token
                        ))
                    );
                }

                return new MultiFieldException(
                    "MismatchedInputException",
                    List.of(new FieldError(
                        path,
                        ex.getMessage().startsWith("Cannot coerce")
                            ? ex.getMessage().split("\n")[0]
                            .replaceFirst(
                                "^Cannot coerce (\\S+) value \\((.+)\\) to `(?:\\w+\\.)*(\\w+)`.*$",
                                "Expected $3, got $1 value $2"
                            )
                            : ex.getMessage()
                    ))
                );
            }

            case InputCoercionException ex -> {
                return new MultiFieldException(
                    "InputCoercionException",
                    List.of(new FieldError(path, ex.getMessage().split("\n")[0]))
                );
            }

            case JsonParseException ex -> {
                return HttpException.badRequest("JsonParseException: " + ex.getMessage());
            }

            default -> {
                return HttpException.internalServerError(e);
            }
        }
    }

    public ApiException fromValidationErrors(Set<ConstraintViolation<ConfigUpdate>> errors) {
        return new MultiFieldException(
            "Errors in some fields",
            errors.stream()
                .map(error -> new FieldError(error.getPropertyPath().toString(), error.getMessage()))
                .toList()
        );
    }

    public ApiException fromValidationErrors(List<ObjectError> errors) {
        return new MultiFieldException(
            "Errors in some fields",
            errors.stream()
                .map(error -> {
                    try {
                        final Field violationField = error.getClass().getDeclaredField("violation");
                        violationField.setAccessible(true);
                        final Object violation = violationField.get(error);

                        if (violation instanceof ConstraintViolation<?> constraintViolation) {
                            return new FieldError(
                                constraintViolation.getPropertyPath().toString(),
                                constraintViolation.getMessage()
                            );
                        }
                    } catch (NoSuchFieldException | IllegalAccessException exception) {
                        LOGGER.error(exception.getMessage(), exception);
                    }

                    return new FieldError("unknown", error.getDefaultMessage());
                })
                .toList()
        );
    }

    private String parsePath(List<JsonMappingException.Reference> references) {
        final StringBuilder path = new StringBuilder();

        for (int i = 0; i < references.size(); i++) {
            final JsonMappingException.Reference reference = references.get(i);

            if (reference.getFieldName() != null) {
                if (i > 0) {
                    path.append(".");
                }

                path.append(reference.getFieldName());
            }

            if (reference.getIndex() != -1) {
                path.append("[");
                path.append(reference.getIndex());
                path.append("]");
            }
        }

        return path.toString();
    }
}
