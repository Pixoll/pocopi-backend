package com.pocopi.api.controllers;

import com.pocopi.api.dto.config.Config;
import com.pocopi.api.dto.config.ConfigUpdateWithFiles;
import com.pocopi.api.dto.config.UpdatedConfig;
import com.pocopi.api.services.ConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/config")
public class ConfigController {
    private final ConfigService configService;

    @Autowired
    public ConfigController(ConfigService configService) {
        this.configService = configService;
    }

    @GetMapping("/latest")
    public ResponseEntity<Config> getLastestConfig() {
        final Config response = configService.getLastConfig();
        return ResponseEntity.ok(response);
    }

    @PatchMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UpdatedConfig> updateConfig(@ModelAttribute ConfigUpdateWithFiles request) {
        final UpdatedConfig response = configService.processUpdatedConfig(request);
        return ResponseEntity.ok(response);
    }
}
