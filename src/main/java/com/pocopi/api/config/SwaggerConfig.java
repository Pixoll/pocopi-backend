package com.pocopi.api.config;

import org.springdoc.core.properties.SwaggerUiConfigProperties;
import org.springdoc.core.properties.SwaggerUiOAuthProperties;
import org.springdoc.core.providers.ObjectMapperProvider;
import org.springdoc.webmvc.ui.SwaggerIndexTransformer;
import org.springdoc.webmvc.ui.SwaggerWelcomeCommon;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "springdoc.swagger-ui.enabled", havingValue = "true", matchIfMissing = true)
public class SwaggerConfig {
    @Bean
    public SwaggerIndexTransformer swaggerIndexTransformer(
        SwaggerUiConfigProperties swaggerUiConfig,
        SwaggerUiOAuthProperties swaggerUiOAuthProperties,
        SwaggerWelcomeCommon swaggerWelcomeCommon,
        ObjectMapperProvider objectMapperProvider
    ) {
        return new SwaggerCustomCssInjector(
            swaggerUiConfig,
            swaggerUiOAuthProperties,
            swaggerWelcomeCommon,
            objectMapperProvider
        );
    }
}
