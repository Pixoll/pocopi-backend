package com.pocopi.api.services;

import com.pocopi.api.dto.config.Pattern;
import com.pocopi.api.repositories.PatternRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PatternService {
    private final PatternRepository patternRepository;

    public PatternService(PatternRepository patternRepository) {
        this.patternRepository = patternRepository;
    }

    public List<Pattern> getAll() {
        return patternRepository.findAll().stream()
            .map(pattern -> new Pattern(pattern.getId(), pattern.getName(), pattern.getRegex()))
            .toList();
    }
}
