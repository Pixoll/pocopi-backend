package com.pocopi.api.integration.services.config;

import com.pocopi.api.dto.config.PatternUpdate;
import com.pocopi.api.models.config.ConfigModel;
import com.pocopi.api.models.config.PatternModel;
import com.pocopi.api.repositories.ConfigRepository;
import com.pocopi.api.repositories.PatternRepository;
import com.pocopi.api.services.PatternService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;


import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("integration")
class PatternServiceIT {

    private static final Logger log = LoggerFactory.getLogger(PatternServiceIT.class);

    @Autowired
    private PatternService patternService;

    @Autowired
    private PatternRepository patternRepository;

    @Autowired
    private ConfigRepository configRepository;

    @BeforeEach
    void setUp() {
        // Limpieza opcional si no usas @Transactional en clase,
        // pero con @Transactional en métodos suele bastar.
    }

    @Test
    @Transactional
    void getAllPatterns_WithExistingPatterns_ShouldReturnFromDb() {
        // Arrange
        int initialCount = patternService.getAllPatterns().size();

        PatternModel p1 = PatternModel.builder().name("P1").regex(".*").build();
        PatternModel p2 = PatternModel.builder().name("P2").regex("\\d+").build();

        patternRepository.save(p1);
        patternRepository.save(p2);

        // Act
        var patterns = patternService.getAllPatterns();

        // Assert
        assertEquals(initialCount + 2, patterns.size(), "Debe aumentar en 2 patterns");
        assertTrue(patterns.stream().anyMatch(p -> "P1".equals(p.name())));
        assertTrue(patterns.stream().anyMatch(p -> "P2".equals(p.name())));
    }


    @Test
    @Transactional
    void updatePattern_WithNewPattern_ShouldPersistAndAssign() {
        // Arrange
        ConfigModel config = ConfigModel.builder()
            .title("C1").description("D").informedConsent("I").build();
        configRepository.save(config);

        PatternUpdate newPattern = new PatternUpdate(
            null, "NewP", "^[a-z]+$"
        );

        // Act
        boolean result = patternService.updatePattern(config, newPattern);

        // Assert
        assertTrue(result);

        ConfigModel updatedConfig = configRepository.findByVersion(config.getVersion()).orElseThrow();
        assertNotNull(updatedConfig.getUsernamePattern());
        assertEquals("NewP", updatedConfig.getUsernamePattern().getName());
        assertNotNull(updatedConfig.getUsernamePattern().getId()); // Verifica que tiene ID generado
    }

    @Test
    @Transactional
    void updatePattern_WithExistingPattern_UpdateChanges_ShouldPersistInDb() {
        // Arrange
        PatternModel pattern = PatternModel.builder().name("Old").regex(".*").build();
        patternRepository.save(pattern); // ID generado
        int patternId = pattern.getId(); // Capturamos ID generado por DB

        ConfigModel config = ConfigModel.builder()
            .title("C1").description("D").informedConsent("I").usernamePattern(pattern)
            .build();
        configRepository.save(config);

        PatternUpdate update = new PatternUpdate(
            patternId, "NewName", "\\d+"
        );

        // Act
        boolean result = patternService.updatePattern(config, update);

        // Assert
        assertTrue(result);
        PatternModel updatedPattern = patternRepository.findById(patternId).orElseThrow();
        assertEquals("\\d+", updatedPattern.getRegex());
    }
}
