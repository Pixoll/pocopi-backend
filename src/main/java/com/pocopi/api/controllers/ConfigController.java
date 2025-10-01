package com.pocopi.api.controllers;

import com.pocopi.api.dto.Config.SingleConfigResponse;
import com.pocopi.api.services.interfaces.ConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/config")
public class ConfigController {

    private final ConfigService configService;

    @Autowired
    public ConfigController(ConfigService configService) {
        this.configService = configService;
    }
    @GetMapping("/latest")
    public ResponseEntity<SingleConfigResponse> getLastestConfig() {
        SingleConfigResponse response = configService.getLastConfig();
        return ResponseEntity.ok(response); 
    }
}
