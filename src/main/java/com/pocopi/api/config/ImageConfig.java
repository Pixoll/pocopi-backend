package com.pocopi.api.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class ImageConfig {
    @Getter
    private final String basePath = "./images";

    @Value("${app.images.base-url}")
    private String baseUrl;
}
