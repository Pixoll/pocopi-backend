package com.pocopi.api.controllers;

import com.pocopi.api.dto.config.Pattern;
import com.pocopi.api.services.PatternService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/patterns")
@Tag(name = "Patterns")
public class PatternController {
    private final PatternService patternService;

    public PatternController(PatternService patternService) {
        this.patternService = patternService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<Pattern>> getAllPatterns() {
        final List<Pattern> patterns = patternService.getAllPatterns();
        return ResponseEntity.ok(patterns);
    }
}
