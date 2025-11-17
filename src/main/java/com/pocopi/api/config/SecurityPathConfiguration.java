package com.pocopi.api.config;

import org.springframework.stereotype.Component;

@Component
public class SecurityPathConfiguration {
    public String[] getPermitAllPaths() {
        return new String[]{
            "/actuator",
            "/actuator/**",
            "/api/docs",
            "/api/docs.yaml",
            "/api/docs/**",
            "/api/swagger-ui",
            "/api/swagger-ui*/**",
            "/error",
            "/api/config/active",
            "/api/auth/login",
            "/api/auth/register",
        };
    }
}
