package com.pocopi.api.services;

import com.pocopi.api.dto.api.FieldError;
import com.pocopi.api.dto.config.Pattern;
import com.pocopi.api.dto.config.PatternUpdate;
import com.pocopi.api.exception.MultiFieldException;
import com.pocopi.api.models.config.ConfigModel;
import com.pocopi.api.models.config.PatternModel;
import com.pocopi.api.repositories.ConfigRepository;
import com.pocopi.api.repositories.PatternRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.regex.PatternSyntaxException;

@Service
public class PatternService {
    private final PatternRepository patternRepository;
    private final ConfigRepository configRepository;

    public PatternService(PatternRepository patternRepository, ConfigRepository configRepository) {
        this.patternRepository = patternRepository;
        this.configRepository = configRepository;
    }

    public List<Pattern> getAll() {
        return patternRepository.findAll().stream()
            .map(pattern -> new Pattern(pattern.getId(), pattern.getName(), pattern.getRegex()))
            .toList();
    }

    @Transactional
    public boolean updatePattern(ConfigModel config, PatternUpdate patternUpdate) {
        final PatternModel configUsernamePattern = config.getUsernamePattern();

        if (patternUpdate == null) {
            if (configUsernamePattern != null) {
                config.setUsernamePattern(null);
                configRepository.save(config);
                return true;
            }

            return false;
        }

        try {
            java.util.regex.Pattern.compile(patternUpdate.regex());
        } catch (PatternSyntaxException e) {
            throw new MultiFieldException(
                "Invalid configuration update",
                List.of(new FieldError("usernamePattern", "Invalid Java matching pattern"))
            );
        }

        final PatternModel storedPattern = patternUpdate.id() != null
            ? patternRepository.findById(patternUpdate.id()).orElse(null)
            : null;

        if (storedPattern == null) {
            final PatternModel newPattern = PatternModel.builder()
                .name(patternUpdate.name())
                .regex(patternUpdate.regex())
                .build();

            final PatternModel savedPattern = patternRepository.save(newPattern);

            config.setUsernamePattern(savedPattern);

            configRepository.save(config);
            return true;
        }

        final boolean updated = !Objects.equals(storedPattern.getName(), patternUpdate.name())
            || !Objects.equals(storedPattern.getRegex(), patternUpdate.regex());

        if (!updated) {
            if (configUsernamePattern == null || configUsernamePattern.getId() != patternUpdate.id()) {
                config.setUsernamePattern(storedPattern);
                configRepository.save(config);
                return true;
            }

            return false;
        }

        storedPattern.setName(storedPattern.getName());
        storedPattern.setRegex(patternUpdate.regex());
        patternRepository.save(storedPattern);

        if (configUsernamePattern == null || configUsernamePattern.getId() != patternUpdate.id()) {
            config.setUsernamePattern(storedPattern);
            configRepository.save(config);
        }

        return true;
    }
}
