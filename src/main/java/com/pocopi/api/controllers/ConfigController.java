package com.pocopi.api.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pocopi.api.dto.config.ConfigPreview;
import com.pocopi.api.dto.config.ConfigUpdate;
import com.pocopi.api.dto.config.FullConfig;
import com.pocopi.api.dto.config.TrimmedConfig;
import com.pocopi.api.exception.HttpException;
import com.pocopi.api.mappers.ApiExceptionMapper;
import com.pocopi.api.services.ConfigService;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

import static com.pocopi.api.config.OpenApiCustomizer.SECURITY_SCHEME_NAME;

@RestController
@RequestMapping("/api/configs")
@Tag(name = "Configurations")
public class ConfigController {
    private final ConfigService configService;
    private final ObjectMapper objectMapper;
    private final ApiExceptionMapper apiExceptionMapper;
    private final Validator validator;

    public ConfigController(
        ConfigService configService,
        ObjectMapper objectMapper,
        ApiExceptionMapper apiExceptionMapper,
        Validator validator
    ) {
        this.configService = configService;
        this.objectMapper = objectMapper;
        this.apiExceptionMapper = apiExceptionMapper;
        this.validator = validator;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<ConfigPreview>> getAllConfigs() {
        final List<ConfigPreview> configs = configService.getAllConfigs();
        return ResponseEntity.ok(configs);
    }

    @GetMapping("/{version}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<FullConfig> getConfigByVersion(@PathVariable int version) {
        final FullConfig config = configService.getFullConfigByVersion(version);
        return ResponseEntity.ok(config);
    }

    @DeleteMapping("/{version}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> deleteConfig(@PathVariable int version) {
        configService.deleteConfig(version);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{version}/activate")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> setConfigAsActive(@PathVariable int version) {
        configService.setConfigAsActive(version);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/active")
    public ResponseEntity<TrimmedConfig> getActiveConfigAsUser() {
        final TrimmedConfig config = configService.getTrimmedActiveConfig();
        return ResponseEntity.ok(config);
    }

    @GetMapping("/active/full")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<FullConfig> getActiveConfigAsAdmin() {
        final FullConfig config = configService.getFullActiveConfig();
        return ResponseEntity.ok(config);
    }

    @SecurityRequirement(name = SECURITY_SCHEME_NAME)
    @PatchMapping(
        path = "/active",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> updateActiveConfig(
        @RequestPart(name = "icon", required = false)
        @Schema(description = "New application icon")
        MultipartFile icon,

        @RequestPart(name = "informationCardImages", required = false)
        @Schema(description = "Every image used in the information cards")
        List<MultipartFile> informationCardImages,

        @RequestPart(name = "preTestFormImages", required = false)
        @Schema(description = "Every image used in the pre-test form")
        List<MultipartFile> preTestFormImages,

        @RequestPart(name = "postTestFormImages", required = false)
        @Schema(description = "Every image used in the post-test form")
        List<MultipartFile> postTestFormImages,

        @RequestPart(name = "groupImages", required = false)
        @Schema(description = "Every image used in the test groups")
        List<MultipartFile> groupImages,

        @RequestPart(name = "payload")
        @NotNull
        @Schema(implementation = ConfigUpdate.class, description = "Configuration update")
        String payload
    ) {
        final ConfigUpdate configUpdate = parseUpdateConfigPayload(payload);

        final Set<ConstraintViolation<ConfigUpdate>> errors = validator.validate(configUpdate);

        if (!errors.isEmpty()) {
            throw apiExceptionMapper.fromValidationErrors(errors);
        }

        final boolean modified = configService.updateActiveConfig(
            configUpdate,
            icon,
            informationCardImages != null ? informationCardImages : List.of(),
            preTestFormImages != null ? preTestFormImages : List.of(),
            postTestFormImages != null ? postTestFormImages : List.of(),
            groupImages != null ? groupImages : List.of()
        );

        return new ResponseEntity<>(modified ? HttpStatus.OK : HttpStatus.NOT_MODIFIED);
    }

    private ConfigUpdate parseUpdateConfigPayload(String json) {
        if (json == null || json.isBlank()) {
            throw HttpException.badRequest("Config update payload json cannot be empty");
        }

        try {
            return objectMapper.readValue(
                json, new TypeReference<>() {
                }
            );
        } catch (JsonProcessingException e) {
            throw apiExceptionMapper.fromJsonProcessingException(e);
        }
    }
}
