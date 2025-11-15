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
import com.pocopi.api.exception.ApiException;
import com.pocopi.api.exception.HttpException;
import com.pocopi.api.exception.MultiFieldException;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class ApiExceptionMapper {
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

                return HttpException.badRequest("MismatchedInputException: " + ex.getMessage());
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
