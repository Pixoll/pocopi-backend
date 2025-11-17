package com.pocopi.api.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Setter
@Getter
@ConfigurationProperties(prefix = "app.cors")
@Component
public class AppCorsProperties {
    private List<String> allowedOrigins = List.of();
    private List<String> allowedMethods = List.of("GET", "POST", "PATCH", "PUT", "DELETE", "OPTIONS");
    private List<String> allowedHeaders = List.of("*");
    private Boolean allowCredentials = true;
}
