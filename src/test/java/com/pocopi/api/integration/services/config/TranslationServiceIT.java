package com.pocopi.api.integration.services.config;

import com.pocopi.api.dto.config.Translation;
import com.pocopi.api.dto.config.TranslationUpdate;
import com.pocopi.api.exception.HttpException;
import com.pocopi.api.models.config.ConfigModel;
import com.pocopi.api.models.config.TranslationKeyModel;
import com.pocopi.api.models.config.TranslationValueModel;
import com.pocopi.api.repositories.ConfigRepository;
import com.pocopi.api.repositories.TranslationKeyRepository;
import com.pocopi.api.repositories.TranslationValueRepository;
import com.pocopi.api.services.TranslationService;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("integration")
class TranslationServiceIT {

    private static final Logger log = LoggerFactory.getLogger(TranslationServiceIT.class);

    @Autowired
    private TranslationService translationService;

    @Autowired
    private TranslationValueRepository translationValueRepository;

    @Autowired
    private TranslationKeyRepository translationKeyRepository;

    @Autowired
    private ConfigRepository configRepository;

    // ==================== getAllTranslationKeyValues Tests ====================

    @Test
    @Transactional
    void getAllTranslationKeyValues_WithExistingTranslations_ShouldReturnMap() {
        log.info("----------- Iniciando TranslationServiceIT.getAllTranslationKeyValues_WithExistingTranslations_ShouldReturnMap -----------");

        // Arrange
        ConfigModel config = configRepository.save(ConfigModel.builder()
            .title("Test").description("Test").informedConsent("Test").build());

        TranslationKeyModel key1 = translationKeyRepository.save(TranslationKeyModel.builder()
            .key("test.key1").value("Description 1").arguments(List.of()).build());

        TranslationKeyModel key2 = translationKeyRepository.save(TranslationKeyModel.builder()
            .key("test.key2").value("Description 2").arguments(List.of()).build());

        translationValueRepository.save(TranslationValueModel.builder()
            .config(config).key(key1).value("Value 1").build());

        translationValueRepository.save(TranslationValueModel.builder()
            .config(config).key(key2).value("Value 2").build());

        // Act
        Map<String, String> result = translationService.getAllTranslationKeyValues(config.getVersion());

        // Assert
        assertTrue(result.size() >= 2, "Debe contener al menos 2 traducciones");
        assertEquals("Value 1", result.get("test.key1"), "test.key1 debe tener el valor correcto");
        assertEquals("Value 2", result.get("test.key2"), "test.key2 debe tener el valor correcto");

        log.info("----------- Finalizó correctamente TranslationServiceIT.getAllTranslationKeyValues_WithExistingTranslations_ShouldReturnMap -----------");
    }

    // ==================== getAllTranslations Tests ====================

    @Test
    @Transactional
    void getAllTranslations_WithExistingTranslations_ShouldReturnDtoList() {
        log.info("----------- Iniciando TranslationServiceIT.getAllTranslations_WithExistingTranslations_ShouldReturnDtoList -----------");

        // Arrange
        ConfigModel config = configRepository.save(ConfigModel.builder()
            .title("Test").description("Test").informedConsent("Test").build());

        TranslationKeyModel key = translationKeyRepository.save(TranslationKeyModel.builder()
            .key("test.welcome")
            .value("Welcome message")
            .arguments(List.of("username"))
            .build());

        translationValueRepository.save(TranslationValueModel.builder()
            .config(config).key(key).value("Welcome {0}!").build());

        // Act
        List<Translation> result = translationService.getAllTranslations(config.getVersion());

        // Assert
        assertTrue(result.size() >= 1, "Debe contener al menos 1 traducción");

        Translation testWelcome = result.stream()
            .filter(t -> "test.welcome".equals(t.key()))
            .findFirst()
            .orElseThrow(() -> new AssertionError("Debe contener test.welcome"));

        assertEquals("Welcome {0}!", testWelcome.value());
        assertEquals("Welcome message", testWelcome.description());
        assertEquals(List.of("username"), testWelcome.arguments());

        log.info("----------- Finalizó correctamente TranslationServiceIT.getAllTranslations_WithExistingTranslations_ShouldReturnDtoList -----------");
    }


    // ==================== updateTranslations Tests ====================

    @Test
    @Transactional
    void updateTranslations_WithNonExistentKey_ShouldThrowNotFoundException() {
        log.info("----------- Iniciando TranslationServiceIT.updateTranslations_WithNonExistentKey_ShouldThrowNotFoundException -----------");

        // Arrange
        ConfigModel config = configRepository.save(ConfigModel.builder()
            .title("Test").description("Test").informedConsent("Test").build());

        TranslationUpdate update = new TranslationUpdate("nonexistent.key", "Value");

        // Act & Assert
        HttpException exception = assertThrows(HttpException.class,
            () -> translationService.updateTranslations(config, List.of(update)));

        assertTrue(exception.getMessage().contains("Translation key nonexistent.key not found"));

        log.info("----------- Finalizó correctamente TranslationServiceIT.updateTranslations_WithNonExistentKey_ShouldThrowNotFoundException -----------");
    }

    @Test
    @Transactional
    void updateTranslations_WithNewTranslation_ShouldCreateInDb() {
        log.info("----------- Iniciando TranslationServiceIT.updateTranslations_WithNewTranslation_ShouldCreateInDb -----------");

        // Arrange
        ConfigModel config = configRepository.save(ConfigModel.builder()
            .title("Test").description("Test").informedConsent("Test").build());

        TranslationKeyModel key = translationKeyRepository.save(TranslationKeyModel.builder()
            .key("test.new").value("New translation").arguments(List.of()).build());

        TranslationUpdate update = new TranslationUpdate("test.new", "New Value");

        // Act
        boolean result = translationService.updateTranslations(config, List.of(update));

        // Assert
        assertTrue(result);
        List<TranslationValueModel> translations = translationValueRepository
            .findAllByConfigVersion(config.getVersion());
        assertEquals(1, translations.size());
        assertEquals("New Value", translations.get(0).getValue());

        log.info("----------- Finalizó correctamente TranslationServiceIT.updateTranslations_WithNewTranslation_ShouldCreateInDb -----------");
    }

    @Test
    @Transactional
    void updateTranslations_WithExistingTranslation_AndChanges_ShouldUpdateInDb() {
        log.info("----------- Iniciando TranslationServiceIT.updateTranslations_WithExistingTranslation_AndChanges_ShouldUpdateInDb -----------");

        // Arrange
        ConfigModel config = configRepository.save(ConfigModel.builder()
            .title("Test").description("Test").informedConsent("Test").build());

        TranslationKeyModel key = translationKeyRepository.save(TranslationKeyModel.builder()
            .key("test.update").value("Update translation").arguments(List.of()).build());

        translationValueRepository.save(TranslationValueModel.builder()
            .config(config).key(key).value("Old Value").build());

        TranslationUpdate update = new TranslationUpdate("test.update", "Updated Value");

        // Act
        boolean result = translationService.updateTranslations(config, List.of(update));

        // Assert
        assertTrue(result);
        List<TranslationValueModel> translations = translationValueRepository
            .findAllByConfigVersion(config.getVersion());
        assertEquals(1, translations.size());
        assertEquals("Updated Value", translations.get(0).getValue());

        log.info("----------- Finalizó correctamente TranslationServiceIT.updateTranslations_WithExistingTranslation_AndChanges_ShouldUpdateInDb -----------");
    }

    @Test
    @Transactional
    void updateTranslations_WithExistingTranslation_AndNoChanges_ShouldReturnFalse() {
        log.info("----------- Iniciando TranslationServiceIT.updateTranslations_WithExistingTranslation_AndNoChanges_ShouldReturnFalse -----------");

        // Arrange
        ConfigModel config = configRepository.save(ConfigModel.builder()
            .title("Test").description("Test").informedConsent("Test").build());

        TranslationKeyModel key = translationKeyRepository.save(TranslationKeyModel.builder()
            .key("test.same").value("Same translation").arguments(List.of()).build());

        translationValueRepository.save(TranslationValueModel.builder()
            .config(config).key(key).value("Same Value").build());

        TranslationUpdate update = new TranslationUpdate("test.same", "Same Value");

        // Act
        boolean result = translationService.updateTranslations(config, List.of(update));

        // Assert
        assertFalse(result);

        log.info("----------- Finalizó correctamente TranslationServiceIT.updateTranslations_WithExistingTranslation_AndNoChanges_ShouldReturnFalse -----------");
    }

    // ==================== cloneTranslations Tests ====================

    @Test
    @Transactional
    void cloneTranslations_WithExistingTranslations_ShouldCreateCopiesInNewConfig() {
        log.info("----------- Iniciando TranslationServiceIT.cloneTranslations_WithExistingTranslations_ShouldCreateCopiesInNewConfig -----------");

        // Arrange
        ConfigModel originalConfig = configRepository.save(ConfigModel.builder()
            .title("Original").description("Original").informedConsent("Original").build());

        ConfigModel newConfig = configRepository.save(ConfigModel.builder()
            .title("New").description("New").informedConsent("New").build());

        TranslationKeyModel key = translationKeyRepository.save(TranslationKeyModel.builder()
            .key("test.clone").value("Clone translation").arguments(List.of()).build());

        translationValueRepository.save(TranslationValueModel.builder()
            .config(originalConfig).key(key).value("Original Value").build());

        // Act
        translationService.cloneTranslations(originalConfig.getVersion(), newConfig);

        // Assert
        List<TranslationValueModel> originalTranslations = translationValueRepository
            .findAllByConfigVersion(originalConfig.getVersion());
        List<TranslationValueModel> clonedTranslations = translationValueRepository
            .findAllByConfigVersion(newConfig.getVersion());

        assertEquals(1, originalTranslations.size());
        assertEquals(1, clonedTranslations.size());
        assertEquals("Original Value", clonedTranslations.get(0).getValue());
        assertEquals(newConfig.getVersion(), clonedTranslations.get(0).getConfig().getVersion());
        assertNotEquals(originalTranslations.get(0).getId(), clonedTranslations.get(0).getId());

        log.info("----------- Finalizó correctamente TranslationServiceIT.cloneTranslations_WithExistingTranslations_ShouldCreateCopiesInNewConfig -----------");
    }
}
