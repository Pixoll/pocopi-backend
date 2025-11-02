package com.pocopi.api.controllers;

import com.pocopi.api.dto.config.FullConfig;
import com.pocopi.api.dto.config.ConfigUpdateWithFiles;
import com.pocopi.api.dto.config.TrimmedConfig;
import com.pocopi.api.dto.config.UpdatedConfig;
import com.pocopi.api.services.ConfigService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/config")
@Tag(name = "Config")
public class ConfigController {
    private final ConfigService configService;

    public ConfigController(ConfigService configService) {
        this.configService = configService;
    }

    @GetMapping("/latest")
    public ResponseEntity<TrimmedConfig> getLastestConfigAsUser() {
        final TrimmedConfig config = configService.getLatestConfigTrimmed();
        return ResponseEntity.ok(config);
    }

    @GetMapping("/latest/full")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FullConfig> getLastestConfigAsAdmin() {
        final FullConfig config = configService.getLatestConfigFull();
        return ResponseEntity.ok(config);
    }

    @PatchMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UpdatedConfig> updateConfig(@ModelAttribute ConfigUpdateWithFiles request) {
        final UpdatedConfig updatedConfig = configService.updateConfig(request);
        return ResponseEntity.ok(updatedConfig);
    }
}
