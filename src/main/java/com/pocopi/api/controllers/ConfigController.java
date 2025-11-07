package com.pocopi.api.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pocopi.api.dto.config.ConfigUpdate;
import com.pocopi.api.dto.config.FullConfig;
import com.pocopi.api.dto.config.TrimmedConfig;
import com.pocopi.api.exception.HttpException;
import com.pocopi.api.services.ConfigService;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static com.pocopi.api.config.OpenApiCustomizer.SECURITY_SCHEME_NAME;

@RestController
@RequestMapping("/api/config")
@Tag(name = "Config")
public class ConfigController {
    private final static ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final ConfigService configService;

    public ConfigController(ConfigService configService) {
        this.configService = configService;
    }

    @GetMapping
    public ResponseEntity<TrimmedConfig> getLastestConfigAsUser() {
        final TrimmedConfig config = configService.getLatestConfigTrimmed();
        return ResponseEntity.ok(config);
    }

    @GetMapping("/full")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<FullConfig> getLastestConfigAsAdmin() {
        final FullConfig config = configService.getLatestConfigFull();
        return ResponseEntity.ok(config);
    }

    @SecurityRequirement(name = SECURITY_SCHEME_NAME)
    @PatchMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> updateLatestConfig(
        @RequestPart(name = "icon", required = false)
        @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED, description = "New application icon")
        MultipartFile icon,

        @RequestPart(name = "informationCardImages", required = false)
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Every image used in the information cards")
        List<MultipartFile> informationCardImages,

        @RequestPart(name = "preTestFormImages", required = false)
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Every image used in the pre-test form")
        List<MultipartFile> preTestFormImages,

        @RequestPart(name = "postTestFormImages", required = false)
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Every image used in the post-test form")
        List<MultipartFile> postTestFormImages,

        @RequestPart(name = "groupImages", required = false)
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Every image used in the test groups")
        List<MultipartFile> groupImages,

        @RequestPart(name = "payload")
        @Schema(
            implementation = ConfigUpdate.class,
            requiredMode = Schema.RequiredMode.REQUIRED,
            description = "Configuration update"
        )
        String payload
    ) {
        final ConfigUpdate configUpdate = parseUpdateConfigPayload(payload);

        final boolean modified = configService.updateLatestConfig(
            configUpdate,
            icon,
            informationCardImages,
            preTestFormImages,
            postTestFormImages,
            groupImages
        );

        return new ResponseEntity<>(modified ? HttpStatus.OK : HttpStatus.NOT_MODIFIED);
    }

    @SuppressWarnings("JvmTaintAnalysis")
    private static ConfigUpdate parseUpdateConfigPayload(String json) {
        if (json == null || json.isBlank()) {
            throw HttpException.badRequest("Config update payload json cannot be empty");
        }

        try {
            return OBJECT_MAPPER.readValue(
                json, new TypeReference<>() {
                }
            );
        } catch (JsonProcessingException e) {
            throw HttpException.badRequest("Invalid config payload json: " + e.getMessage());
        }
    }
}
