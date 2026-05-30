package com.pocopi.api.config;

import com.pocopi.api.dto.api.ApiHttpError;
import com.pocopi.api.dto.form.FormQuestionUpdate;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.GlobalOpenApiCustomizer;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springdoc.core.utils.SpringDocAnnotationsUtils;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;

@Component
public class OpenApiCustomizer implements GlobalOpenApiCustomizer, OperationCustomizer {
    private static final String VALIDATION_ERROR_CODE = String.valueOf(HttpStatus.UNPROCESSABLE_ENTITY.value());
    private static final String UNAUTHORIZED_CODE = String.valueOf(HttpStatus.UNAUTHORIZED.value());
    private static final String FORBIDDEN_CODE = String.valueOf(HttpStatus.FORBIDDEN.value());
    private static final String SECURITY_SCHEME_NAME = "bearerAuth";
    private Schema<?> errorSchema = null;

    @Override
    public void customise(OpenAPI openApi) {
        if (openApi.getPaths() == null) {
            return;
        }

        this.errorSchema = SpringDocAnnotationsUtils.resolveSchemaFromType(
            ApiHttpError.class,
            openApi.getComponents(),
            null,
            true
        );

        final Components components = openApi.getComponents();

        final Schema<?> formQuestionUpdateSchema = components.getSchemas()
            .get(FormQuestionUpdate.class.getSimpleName());

        if (formQuestionUpdateSchema != null) {
            formQuestionUpdateSchema.setDiscriminator(null);
            formQuestionUpdateSchema.setProperties(null);
            formQuestionUpdateSchema.setRequired(null);
        }

        components.addSecuritySchemes(
            SECURITY_SCHEME_NAME,
            new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("JWT authentication token")
        );
    }

    public Operation customize(Operation operation, HandlerMethod handlerMethod) {
        if (operation.getRequestBody() != null) {
            addValidationErrorResponse(operation);
        }

        final boolean isProtected = handlerMethod.hasMethodAnnotation(PreAuthorize.class)
            || handlerMethod.getBeanType().isAnnotationPresent(PreAuthorize.class);

        if (isProtected) {
            addSecurityRequirement(operation);
            addAuthErrorResponses(operation);
        }

        return operation;
    }

    private void addSecurityRequirement(Operation operation) {
        if (operation.getSecurity() == null || operation.getSecurity().isEmpty()) {
            operation.addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME));
        }
    }

    private void addValidationErrorResponse(Operation operation) {
        final ApiResponses responses = operation.getResponses();
        if (responses.get(VALIDATION_ERROR_CODE) != null) {
            return;
        }

        final ApiResponse errorResponse = new ApiResponse()
            .description("Validation error")
            .content(new Content()
                .addMediaType("application/json", new MediaType().schema(this.errorSchema))
            );

        responses.addApiResponse(VALIDATION_ERROR_CODE, errorResponse);
    }

    private void addAuthErrorResponses(Operation operation) {
        final ApiResponses responses = operation.getResponses();

        if (responses.get(UNAUTHORIZED_CODE) == null) {
            final ApiResponse unauthorizedResponse = new ApiResponse()
                .description("Unauthorized")
                .content(new Content()
                    .addMediaType("application/json", new MediaType().schema(this.errorSchema))
                );

            responses.addApiResponse(UNAUTHORIZED_CODE, unauthorizedResponse);
        }

        if (responses.get(FORBIDDEN_CODE) == null) {
            final ApiResponse forbiddenResponse = new ApiResponse()
                .description("Forbidden")
                .content(new Content()
                    .addMediaType("application/json", new MediaType().schema(this.errorSchema))
                );

            responses.addApiResponse(FORBIDDEN_CODE, forbiddenResponse);
        }
    }
}
