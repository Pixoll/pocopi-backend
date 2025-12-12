package com.pocopi.api.unit.services.config;

import com.pocopi.api.dto.config.Translation;
import com.pocopi.api.dto.config.TranslationUpdate;
import com.pocopi.api.exception.HttpException;
import com.pocopi.api.models.config.ConfigModel;
import com.pocopi.api.models.config.TranslationKeyModel;
import com.pocopi.api.models.config.TranslationValueModel;
import com.pocopi.api.repositories.TranslationKeyRepository;
import com.pocopi.api.repositories.TranslationValueRepository;
import com.pocopi.api.repositories.projections.TranslationProjection;
import com.pocopi.api.repositories.projections.TranslationWithDetailsProjection;
import com.pocopi.api.services.TranslationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TranslationServiceTest {

    @Mock
    private TranslationValueRepository translationValueRepository;

    @Mock
    private TranslationKeyRepository translationKeyRepository;

    private TranslationService translationService;

    @BeforeEach
    void setUp() {
        translationService = new TranslationService(translationValueRepository, translationKeyRepository);
    }

    // ==================== getAllTranslationKeyValues Tests ====================

    @Test
    void getAllTranslationKeyValues_WithExistingTranslations_ShouldReturnMap() {
        // Arrange
        int configVersion = 1;
        TranslationProjection proj1 = createProjection("home.title", "Home Page");
        TranslationProjection proj2 = createProjection("home.subtitle", "Welcome");

        when(translationValueRepository.findAllKeyValuePairsByConfigVersion(configVersion))
            .thenReturn(List.of(proj1, proj2));

        // Act
        Map<String, String> result = translationService.getAllTranslationKeyValues(configVersion);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Home Page", result.get("home.title"));
        assertEquals("Welcome", result.get("home.subtitle"));
        verify(translationValueRepository, times(1)).findAllKeyValuePairsByConfigVersion(configVersion);
    }

    @Test
    void getAllTranslationKeyValues_WithEmptyTranslations_ShouldReturnEmptyMap() {
        // Arrange
        int configVersion = 1;
        when(translationValueRepository.findAllKeyValuePairsByConfigVersion(configVersion))
            .thenReturn(List.of());

        // Act
        Map<String, String> result = translationService.getAllTranslationKeyValues(configVersion);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ==================== getAllTranslations Tests ====================

    @Test
    void getAllTranslations_WithExistingTranslations_ShouldReturnDtoList() {
        // Arrange
        int configVersion = 1;
        TranslationWithDetailsProjection proj = createDetailProjection(
            "home.welcome",
            "Welcome {0}!",
            "Welcome message with username",
            "[\"username\"]"
        );

        when(translationValueRepository.findAllByConfigVersionWithDetails(configVersion))
            .thenReturn(List.of(proj));

        // Act
        List<Translation> result = translationService.getAllTranslations(configVersion);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("home.welcome", result.get(0).key());
        assertEquals("Welcome {0}!", result.get(0).value());
        assertEquals("Welcome message with username", result.get(0).description());
        assertEquals(List.of("username"), result.get(0).arguments());
    }

    @Test
    void getAllTranslations_WithNullArgumentsJson_ShouldReturnEmptyList() {
        // Arrange
        int configVersion = 1;
        TranslationWithDetailsProjection proj = createDetailProjection(
            "home.title",
            "Home",
            "Home page title",
            null
        );

        when(translationValueRepository.findAllByConfigVersionWithDetails(configVersion))
            .thenReturn(List.of(proj));

        // Act
        List<Translation> result = translationService.getAllTranslations(configVersion);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).arguments().isEmpty());
    }

    // ==================== updateTranslations Tests ====================

    @Test
    void updateTranslations_WithNonExistentKey_ShouldThrowNotFoundException() {
        // Arrange
        ConfigModel config = ConfigModel.builder().version(1).build();
        TranslationUpdate update = new TranslationUpdate("invalid.key", "Value");

        when(translationKeyRepository.findAll()).thenReturn(List.of());

        // Act & Assert
        HttpException exception = assertThrows(HttpException.class,
            () -> translationService.updateTranslations(config, List.of(update)));

        assertTrue(exception.getMessage().contains("Translation key invalid.key not found"));
    }

    @Test
    void updateTranslations_WithNewTranslation_ShouldSaveAndReturnTrue() {
        // Arrange
        ConfigModel config = ConfigModel.builder().version(1).build();
        TranslationKeyModel key = TranslationKeyModel.builder()
            .id(1)
            .key("home.title")
            .build();

        TranslationUpdate update = new TranslationUpdate("home.title", "New Title");

        when(translationKeyRepository.findAll()).thenReturn(List.of(key));
        when(translationValueRepository.findAllByConfigVersion(1)).thenReturn(List.of());

        // Act
        boolean result = translationService.updateTranslations(config, List.of(update));

        // Assert
        assertTrue(result);
        verify(translationValueRepository, times(1)).save(any(TranslationValueModel.class));
    }

    @Test
    void updateTranslations_WithExistingTranslation_AndNoChanges_ShouldReturnFalse() {
        // Arrange
        ConfigModel config = ConfigModel.builder().version(1).build();
        TranslationKeyModel key = TranslationKeyModel.builder()
            .id(1)
            .key("home.title")
            .build();

        TranslationValueModel storedValue = TranslationValueModel.builder()
            .id(1)
            .config(config)
            .key(key)
            .value("Home")
            .build();

        TranslationUpdate update = new TranslationUpdate("home.title", "Home");

        when(translationKeyRepository.findAll()).thenReturn(List.of(key));
        when(translationValueRepository.findAllByConfigVersion(1)).thenReturn(List.of(storedValue));

        // Act
        boolean result = translationService.updateTranslations(config, List.of(update));

        // Assert
        assertFalse(result);
        verify(translationValueRepository, never()).save(any());
    }

    @Test
    void updateTranslations_WithExistingTranslation_AndChanges_ShouldUpdateAndReturnTrue() {
        // Arrange
        ConfigModel config = ConfigModel.builder().version(1).build();
        TranslationKeyModel key = TranslationKeyModel.builder()
            .id(1)
            .key("home.title")
            .build();

        TranslationValueModel storedValue = TranslationValueModel.builder()
            .id(1)
            .config(config)
            .key(key)
            .value("Old Title")
            .build();

        TranslationUpdate update = new TranslationUpdate("home.title", "New Title");

        when(translationKeyRepository.findAll()).thenReturn(List.of(key));
        when(translationValueRepository.findAllByConfigVersion(1)).thenReturn(List.of(storedValue));

        // Act
        boolean result = translationService.updateTranslations(config, List.of(update));

        // Assert
        assertTrue(result);
        verify(translationValueRepository, times(1)).save(storedValue);
        assertEquals("New Title", storedValue.getValue());
    }

    // ==================== cloneTranslations Tests ====================

    @Test
    void cloneTranslations_WithExistingTranslations_ShouldCloneAll() {
        // Arrange
        int originalConfigVersion = 1;
        ConfigModel newConfig = ConfigModel.builder().version(2).build();

        TranslationKeyModel key = TranslationKeyModel.builder()
            .id(1)
            .key("home.title")
            .build();

        TranslationValueModel original = TranslationValueModel.builder()
            .id(1)
            .key(key)
            .value("Original Title")
            .build();

        when(translationValueRepository.findAllByConfigVersion(originalConfigVersion))
            .thenReturn(List.of(original));

        // Act
        translationService.cloneTranslations(originalConfigVersion, newConfig);

        // Assert
        verify(translationValueRepository, times(1)).save(any(TranslationValueModel.class));
    }

    // ==================== Helper Methods ====================

    private TranslationProjection createProjection(String key, String value) {
        return new TranslationProjection() {
            @Override
            public String getKey() {
                return key;
            }

            @Override
            public String getValue() {
                return value;
            }
        };
    }

    private TranslationWithDetailsProjection createDetailProjection(
        String key,
        String value,
        String description,
        String argumentsJson
    ) {
        return new TranslationWithDetailsProjection() {
            @Override
            public String getKey() {
                return key;
            }

            @Override
            public String getValue() {
                return value;
            }

            @Override
            public String getDescription() {
                return description;
            }

            @Override
            public String getArgumentsJson() {
                return argumentsJson;
            }
        };
    }
}
