package com.pocopi.api.config;

import com.pocopi.api.dto.api.ApiHttpError;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.springdoc.core.customizers.GlobalOpenApiCustomizer;
import org.springdoc.core.utils.SpringDocAnnotationsUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class OpenApiCustomizer implements GlobalOpenApiCustomizer {
    private static final String VALIDATION_ERROR_CODE = String.valueOf(HttpStatus.UNPROCESSABLE_ENTITY.value());

    @Override
    public void customise(OpenAPI openApi) {
        if (openApi.getPaths() == null) {
            return;
        }

        final Schema<?> schema = SpringDocAnnotationsUtils.resolveSchemaFromType(
            ApiHttpError.class,
            openApi.getComponents(),
            null,
            true
        );

        openApi.getPaths().forEach((path, pathItem) ->
            pathItem.readOperations().forEach(operation -> {
                if (operation.getRequestBody() != null) {
                    addValidationErrorResponse(operation, schema);
                }
            })
        );
    }

    private void addValidationErrorResponse(Operation operation, Schema<?> schema) {
        final ApiResponses responses = operation.getResponses();
        if (responses.get(VALIDATION_ERROR_CODE) != null) {
            return;
        }

        final ApiResponse errorResponse = new ApiResponse()
            .description("Validation error")
            .content(new Content()
                .addMediaType("application/json", new MediaType().schema(schema))
            );

        responses.addApiResponse(VALIDATION_ERROR_CODE, errorResponse);
    }
}
