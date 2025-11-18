package com.pocopi.api.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springdoc.core.properties.SwaggerUiConfigProperties;
import org.springdoc.core.properties.SwaggerUiOAuthProperties;
import org.springdoc.core.providers.ObjectMapperProvider;
import org.springdoc.webmvc.ui.SwaggerIndexPageTransformer;
import org.springdoc.webmvc.ui.SwaggerWelcomeCommon;
import org.springframework.core.io.Resource;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.resource.ResourceTransformerChain;
import org.springframework.web.servlet.resource.TransformedResource;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

public class SwaggerCustomCssInjector extends SwaggerIndexPageTransformer {
    public SwaggerCustomCssInjector(
        final SwaggerUiConfigProperties swaggerUiConfig,
        final SwaggerUiOAuthProperties swaggerUiOAuthProperties,
        final SwaggerWelcomeCommon swaggerWelcomeCommon,
        final ObjectMapperProvider objectMapperProvider
    ) {
        super(swaggerUiConfig, swaggerUiOAuthProperties, swaggerWelcomeCommon, objectMapperProvider);
    }

    @Override
    public @NonNull Resource transform(
        @NonNull HttpServletRequest request,
        @NonNull Resource resource,
        @NonNull ResourceTransformerChain transformer
    ) throws IOException {
        if (Objects.equals(resource.getFilename(), "swagger-ui.css")) {
            final String newCss;

            try (final InputStream inputStream = this.getClass().getResourceAsStream("/static/swagger-theme.css")) {
                newCss = inputStream != null ? new String(inputStream.readAllBytes()) : "";
            }

            try (final InputStream inputStream = resource.getInputStream()) {
                final String css = new String(inputStream.readAllBytes());
                final String transformedCss = css + "\n" + newCss;
                return new TransformedResource(resource, transformedCss.getBytes());
            }
        }

        return super.transform(request, resource, transformer);
    }
}
