package com.pocopi.api.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.cfg.CoercionAction;
import com.fasterxml.jackson.databind.cfg.CoercionInputShape;
import com.fasterxml.jackson.databind.type.LogicalType;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jsonCustomizer() {
        return builder -> {
            builder.featuresToEnable(
                DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES,
                DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES
            );
            builder.featuresToDisable(MapperFeature.ALLOW_COERCION_OF_SCALARS);

            builder.postConfigurer(objectMapper ->
                objectMapper.coercionConfigFor(LogicalType.Textual)
                    .setCoercion(CoercionInputShape.Array, CoercionAction.Fail)
                    .setCoercion(CoercionInputShape.Binary, CoercionAction.Fail)
                    .setCoercion(CoercionInputShape.Boolean, CoercionAction.Fail)
                    .setCoercion(CoercionInputShape.EmptyArray, CoercionAction.Fail)
                    .setCoercion(CoercionInputShape.EmptyObject, CoercionAction.Fail)
                    .setCoercion(CoercionInputShape.Float, CoercionAction.Fail)
                    .setCoercion(CoercionInputShape.Integer, CoercionAction.Fail)
                    .setCoercion(CoercionInputShape.Object, CoercionAction.Fail)
            );
        };
    }
}
