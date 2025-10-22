package com.pocopi.api.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class ImageConfig {
    @Value("${app.images.base-path}")
    private String basePath;

    @Value("${app.images.base-url}")
    private String baseUrl;
}
