package com.pocopi.api.integration.services.config;

import com.pocopi.api.dto.config.FrequentlyAskedQuestion;
import com.pocopi.api.dto.config.FrequentlyAskedQuestionUpdate;
import com.pocopi.api.models.config.ConfigModel;
import com.pocopi.api.models.config.HomeFaqModel;
import com.pocopi.api.repositories.ConfigRepository;
import com.pocopi.api.repositories.HomeFaqRepository;
import com.pocopi.api.services.HomeFaqService;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("integration")
class HomeFaqServiceIT {

    private static final Logger log = LoggerFactory.getLogger(HomeFaqServiceIT.class);

    @Autowired
    private HomeFaqService homeFaqService;

    @Autowired
    private HomeFaqRepository homeFaqRepository;

    @Autowired
    private ConfigRepository configRepository;

    // ==================== getFaqsByConfigVersion Tests ====================

    @Test
    @Transactional
    void getFaqsByConfigVersion_WithExistingFaqs_ShouldReturnCorrectDtos() {
        log.info("----------- Iniciando HomeFaqServiceIT.getFaqsByConfigVersion_WithExistingFaqs_ShouldReturnCorrectDtos -----------");

        // Arrange
        ConfigModel config = ConfigModel.builder()
            .title("Test Config")
            .description("Test Description")
            .informedConsent("Test Consent")
            .build();
        ConfigModel savedConfig = configRepository.save(config);

        HomeFaqModel faq1 = HomeFaqModel.builder()
            .config(savedConfig)
            .order((short) 0)
            .question("What is this?")
            .answer("This is a test")
            .build();

        HomeFaqModel faq2 = HomeFaqModel.builder()
            .config(savedConfig)
            .order((short) 1)
            .question("How does it work?")
            .answer("It works great")
            .build();

        homeFaqRepository.save(faq1);
        homeFaqRepository.save(faq2);

        // Act
        List<FrequentlyAskedQuestion> result = homeFaqService.getFaqsByConfigVersion(savedConfig.getVersion());

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("What is this?", result.get(0).question());
        assertEquals("This is a test", result.get(0).answer());
        assertEquals("How does it work?", result.get(1).question());
        assertEquals("It works great", result.get(1).answer());

        log.info("----------- Finalizó correctamente HomeFaqServiceIT.getFaqsByConfigVersion_WithExistingFaqs_ShouldReturnCorrectDtos -----------");
    }

    @Test
    @Transactional
    void getFaqsByConfigVersion_WithNoFaqs_ShouldReturnEmptyList() {
        log.info("----------- Iniciando HomeFaqServiceIT.getFaqsByConfigVersion_WithNoFaqs_ShouldReturnEmptyList -----------");

        // Arrange
        ConfigModel config = ConfigModel.builder()
            .title("Empty Config")
            .description("No FAQs")
            .informedConsent("Consent")
            .build();
        ConfigModel savedConfig = configRepository.save(config);

        // Act
        List<FrequentlyAskedQuestion> result = homeFaqService.getFaqsByConfigVersion(savedConfig.getVersion());

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());

        log.info("----------- Finalizó correctamente HomeFaqServiceIT.getFaqsByConfigVersion_WithNoFaqs_ShouldReturnEmptyList -----------");
    }

    // ==================== cloneFaqs Tests ====================

    @Test
    @Transactional
    void cloneFaqs_WithExistingFaqs_ShouldCreateCopiesInNewConfig() {
        log.info("----------- Iniciando HomeFaqServiceIT.cloneFaqs_WithExistingFaqs_ShouldCreateCopiesInNewConfig -----------");

        // Arrange
        ConfigModel originalConfig = ConfigModel.builder()
            .title("Original Config")
            .description("Original")
            .informedConsent("Original Consent")
            .build();
        ConfigModel savedOriginalConfig = configRepository.save(originalConfig);

        HomeFaqModel originalFaq1 = HomeFaqModel.builder()
            .config(savedOriginalConfig)
            .order((short) 0)
            .question("Original Q1?")
            .answer("Original A1")
            .build();

        HomeFaqModel originalFaq2 = HomeFaqModel.builder()
            .config(savedOriginalConfig)
            .order((short) 1)
            .question("Original Q2?")
            .answer("Original A2")
            .build();

        homeFaqRepository.save(originalFaq1);
        homeFaqRepository.save(originalFaq2);

        ConfigModel newConfig = ConfigModel.builder()
            .title("New Config")
            .description("New")
            .informedConsent("New Consent")
            .build();
        ConfigModel savedNewConfig = configRepository.save(newConfig);

        // Act
        homeFaqService.cloneFaqs(savedOriginalConfig.getVersion(), savedNewConfig);

        // Assert
        List<HomeFaqModel> originalFaqs = homeFaqRepository.findAllByConfigVersion(savedOriginalConfig.getVersion());
        List<HomeFaqModel> clonedFaqs = homeFaqRepository.findAllByConfigVersion(savedNewConfig.getVersion());

        assertEquals(2, originalFaqs.size());
        assertEquals(2, clonedFaqs.size());

        assertEquals("Original Q1?", clonedFaqs.getFirst().getQuestion());
        assertEquals("Original A1", clonedFaqs.get(0).getAnswer());
        assertEquals(savedNewConfig.getVersion(), clonedFaqs.get(0).getConfig().getVersion());

        assertEquals("Original Q2?", clonedFaqs.get(1).getQuestion());
        assertEquals("Original A2", clonedFaqs.get(1).getAnswer());
        assertEquals(savedNewConfig.getVersion(), clonedFaqs.get(1).getConfig().getVersion());

        assertNotEquals(originalFaqs.get(0).getId(), clonedFaqs.get(0).getId());
        assertNotEquals(originalFaqs.get(1).getId(), clonedFaqs.get(1).getId());

        log.info("----------- Finalizó correctamente HomeFaqServiceIT.cloneFaqs_WithExistingFaqs_ShouldCreateCopiesInNewConfig -----------");
    }

    @Test
    @Transactional
    void cloneFaqs_WithNoFaqs_ShouldNotCreateAnyFaqs() {
        log.info("----------- Iniciando HomeFaqServiceIT.cloneFaqs_WithNoFaqs_ShouldNotCreateAnyFaqs -----------");

        // Arrange
        ConfigModel originalConfig = ConfigModel.builder()
            .title("Empty Original")
            .description("No FAQs")
            .informedConsent("Consent")
            .build();
        ConfigModel savedOriginalConfig = configRepository.save(originalConfig);

        ConfigModel newConfig = ConfigModel.builder()
            .title("New Config")
            .description("New")
            .informedConsent("New Consent")
            .build();
        ConfigModel savedNewConfig = configRepository.save(newConfig);

        // Act
        homeFaqService.cloneFaqs(savedOriginalConfig.getVersion(), savedNewConfig);

        // Assert
        List<HomeFaqModel> clonedFaqs = homeFaqRepository.findAllByConfigVersion(savedNewConfig.getVersion());
        assertTrue(clonedFaqs.isEmpty());

        log.info("----------- Finalizó correctamente HomeFaqServiceIT.cloneFaqs_WithNoFaqs_ShouldNotCreateAnyFaqs -----------");
    }

    // ==================== updateFaqs Tests ====================

    @Test
    @Transactional
    void updateFaqs_WithNullUpdates_AndNoStoredFaqs_ShouldReturnFalse() {
        log.info("----------- Iniciando HomeFaqServiceIT.updateFaqs_WithNullUpdates_AndNoStoredFaqs_ShouldReturnFalse -----------");

        // Arrange
        ConfigModel config = ConfigModel.builder()
            .title("Test Config")
            .description("Test")
            .informedConsent("Consent")
            .build();
        ConfigModel savedConfig = configRepository.save(config);

        // Act
        boolean result = homeFaqService.updateFaqs(savedConfig, null);

        // Assert
        assertFalse(result);

        log.info("----------- Finalizó correctamente HomeFaqServiceIT.updateFaqs_WithNullUpdates_AndNoStoredFaqs_ShouldReturnFalse -----------");
    }

    @Test
    @Transactional
    void updateFaqs_WithNullUpdates_AndStoredFaqs_ShouldDeleteAllFaqs() {
        log.info("----------- Iniciando HomeFaqServiceIT.updateFaqs_WithNullUpdates_AndStoredFaqs_ShouldDeleteAllFaqs -----------");

        // Arrange
        ConfigModel config = ConfigModel.builder()
            .title("Test Config")
            .description("Test")
            .informedConsent("Consent")
            .build();
        ConfigModel savedConfig = configRepository.save(config);

        HomeFaqModel faq = HomeFaqModel.builder()
            .config(savedConfig)
            .order((short) 0)
            .question("To Delete?")
            .answer("Yes")
            .build();
        HomeFaqModel savedFaq = homeFaqRepository.save(faq);

        // Act
        boolean result = homeFaqService.updateFaqs(savedConfig, null);

        // Assert
        assertTrue(result);
        Optional<HomeFaqModel> found = homeFaqRepository.findById(savedFaq.getId());
        assertFalse(found.isPresent());

        log.info("----------- Finalizó correctamente HomeFaqServiceIT.updateFaqs_WithNullUpdates_AndStoredFaqs_ShouldDeleteAllFaqs -----------");
    }

    @Test
    @Transactional
    void updateFaqs_WithNewFaq_ShouldCreateFaqInDb() {
        log.info("----------- Iniciando HomeFaqServiceIT.updateFaqs_WithNewFaq_ShouldCreateFaqInDb -----------");

        // Arrange
        ConfigModel config = ConfigModel.builder()
            .title("Test Config")
            .description("Test")
            .informedConsent("Consent")
            .build();
        ConfigModel savedConfig = configRepository.save(config);

        FrequentlyAskedQuestionUpdate newFaq = new FrequentlyAskedQuestionUpdate(
            null,
            "New Question?",
            "New Answer"
        );

        // Act
        boolean result = homeFaqService.updateFaqs(savedConfig, List.of(newFaq));

        // Assert
        assertTrue(result);
        List<HomeFaqModel> faqs = homeFaqRepository.findAllByConfigVersion(savedConfig.getVersion());
        assertEquals(1, faqs.size());
        assertEquals("New Question?", faqs.get(0).getQuestion());
        assertEquals("New Answer", faqs.get(0).getAnswer());
        assertEquals(0, faqs.get(0).getOrder());

        log.info("----------- Finalizó correctamente HomeFaqServiceIT.updateFaqs_WithNewFaq_ShouldCreateFaqInDb -----------");
    }

    @Test
    @Transactional
    void updateFaqs_WithExistingFaq_AndNoChanges_ShouldReturnFalseAndNotModify() {
        log.info("----------- Iniciando HomeFaqServiceIT.updateFaqs_WithExistingFaq_AndNoChanges_ShouldReturnFalseAndNotModify -----------");

        // Arrange
        ConfigModel config = ConfigModel.builder()
            .title("Test Config")
            .description("Test")
            .informedConsent("Consent")
            .build();
        ConfigModel savedConfig = configRepository.save(config);

        HomeFaqModel faq = HomeFaqModel.builder()
            .config(savedConfig)
            .order((short) 0)
            .question("Existing?")
            .answer("Yes")
            .build();
        HomeFaqModel savedFaq = homeFaqRepository.save(faq);

        FrequentlyAskedQuestionUpdate update = new FrequentlyAskedQuestionUpdate(
            savedFaq.getId(),
            "Existing?",
            "Yes"
        );

        // Act
        boolean result = homeFaqService.updateFaqs(savedConfig, List.of(update));

        // Assert
        assertFalse(result);
        Optional<HomeFaqModel> found = homeFaqRepository.findById(savedFaq.getId());
        assertTrue(found.isPresent());
        assertEquals("Existing?", found.get().getQuestion());
        assertEquals("Yes", found.get().getAnswer());
        assertEquals(0, found.get().getOrder());

        log.info("----------- Finalizó correctamente HomeFaqServiceIT.updateFaqs_WithExistingFaq_AndNoChanges_ShouldReturnFalseAndNotModify -----------");
    }

    @Test
    @Transactional
    void updateFaqs_WithExistingFaq_AndChanges_ShouldUpdateInDb() {
        log.info("----------- Iniciando HomeFaqServiceIT.updateFaqs_WithExistingFaq_AndChanges_ShouldUpdateInDb -----------");

        // Arrange
        ConfigModel config = ConfigModel.builder()
            .title("Test Config")
            .description("Test")
            .informedConsent("Consent")
            .build();
        ConfigModel savedConfig = configRepository.save(config);

        HomeFaqModel faq = HomeFaqModel.builder()
            .config(savedConfig)
            .order((short) 0)
            .question("Old Question?")
            .answer("Old Answer")
            .build();
        HomeFaqModel savedFaq = homeFaqRepository.save(faq);

        FrequentlyAskedQuestionUpdate update = new FrequentlyAskedQuestionUpdate(
            savedFaq.getId(),
            "Updated Question?",
            "Updated Answer"
        );

        // Act
        boolean result = homeFaqService.updateFaqs(savedConfig, List.of(update));

        // Assert
        assertTrue(result);
        Optional<HomeFaqModel> found = homeFaqRepository.findById(savedFaq.getId());
        assertTrue(found.isPresent());
        assertEquals("Updated Question?", found.get().getQuestion());
        assertEquals("Updated Answer", found.get().getAnswer());

        log.info("----------- Finalizó correctamente HomeFaqServiceIT.updateFaqs_WithExistingFaq_AndChanges_ShouldUpdateInDb -----------");
    }

    @Test
    @Transactional
    void updateFaqs_WithRemovedFaq_ShouldDeleteFromDb() {
        log.info("----------- Iniciando HomeFaqServiceIT.updateFaqs_WithRemovedFaq_ShouldDeleteFromDb -----------");

        // Arrange
        ConfigModel config = ConfigModel.builder()
            .title("Test Config")
            .description("Test")
            .informedConsent("Consent")
            .build();
        ConfigModel savedConfig = configRepository.save(config);

        HomeFaqModel faq1 = HomeFaqModel.builder()
            .config(savedConfig)
            .order((short) 0)
            .question("Keep?")
            .answer("Yes")
            .build();

        HomeFaqModel faq2 = HomeFaqModel.builder()
            .config(savedConfig)
            .order((short) 1)
            .question("Delete?")
            .answer("Yes")
            .build();

        HomeFaqModel savedFaq1 = homeFaqRepository.save(faq1);
        HomeFaqModel savedFaq2 = homeFaqRepository.save(faq2);

        FrequentlyAskedQuestionUpdate update = new FrequentlyAskedQuestionUpdate(
            savedFaq1.getId(),
            "Keep?",
            "Yes"
        );

        // Act
        boolean result = homeFaqService.updateFaqs(savedConfig, List.of(update));

        // Assert
        assertTrue(result);
        Optional<HomeFaqModel> found1 = homeFaqRepository.findById(savedFaq1.getId());
        Optional<HomeFaqModel> found2 = homeFaqRepository.findById(savedFaq2.getId());
        assertTrue(found1.isPresent());
        assertFalse(found2.isPresent());

        log.info("----------- Finalizó correctamente HomeFaqServiceIT.updateFaqs_WithRemovedFaq_ShouldDeleteFromDb -----------");
    }

    @Test
    @Transactional
    void updateFaqs_WithMultipleOperations_ShouldHandleCorrectly() {
        log.info("----------- Iniciando HomeFaqServiceIT.updateFaqs_WithMultipleOperations_ShouldHandleCorrectly -----------");

        // Arrange
        ConfigModel config = ConfigModel.builder()
            .title("Test Config")
            .description("Test")
            .informedConsent("Consent")
            .build();
        ConfigModel savedConfig = configRepository.save(config);

        HomeFaqModel existingFaq = HomeFaqModel.builder()
            .config(savedConfig)
            .order((short) 0)
            .question("Update me?")
            .answer("Old")
            .build();

        HomeFaqModel faqToDelete = HomeFaqModel.builder()
            .config(savedConfig)
            .order((short) 1)
            .question("Delete me?")
            .answer("Yes")
            .build();

        HomeFaqModel savedExisting = homeFaqRepository.save(existingFaq);
        HomeFaqModel savedToDelete = homeFaqRepository.save(faqToDelete);

        List<FrequentlyAskedQuestionUpdate> updates = List.of(
            new FrequentlyAskedQuestionUpdate(
                savedExisting.getId(),
                "Updated Question?",
                "New"
            ),
            new FrequentlyAskedQuestionUpdate(
                null,
                "Brand New?",
                "Yes"
            )
        );

        // Act
        boolean result = homeFaqService.updateFaqs(savedConfig, updates);

        // Assert
        assertTrue(result);
        List<HomeFaqModel> allFaqs = homeFaqRepository.findAllByConfigVersion(savedConfig.getVersion());
        assertEquals(2, allFaqs.size());

        Optional<HomeFaqModel> updated = homeFaqRepository.findById(savedExisting.getId());
        assertTrue(updated.isPresent());
        assertEquals("Updated Question?", updated.get().getQuestion());
        assertEquals("New", updated.get().getAnswer());
        assertEquals(0, updated.get().getOrder());

        HomeFaqModel created = allFaqs.stream()
            .filter(f -> "Brand New?".equals(f.getQuestion()))
            .findFirst()
            .orElse(null);
        assertNotNull(created);
        assertEquals("Yes", created.getAnswer());
        assertEquals(1, created.getOrder());

        Optional<HomeFaqModel> deleted = homeFaqRepository.findById(savedToDelete.getId());
        assertFalse(deleted.isPresent());

        log.info("----------- Finalizó correctamente HomeFaqServiceIT.updateFaqs_WithMultipleOperations_ShouldHandleCorrectly -----------");
    }

    @Test
    @Transactional
    void updateFaqs_WithOrderChange_ShouldUpdateOrder() {
        log.info("----------- Iniciando HomeFaqServiceIT.updateFaqs_WithOrderChange_ShouldUpdateOrder -----------");

        // Arrange
        ConfigModel config = ConfigModel.builder()
            .title("Test Config")
            .description("Test")
            .informedConsent("Consent")
            .build();
        ConfigModel savedConfig = configRepository.save(config);

        HomeFaqModel faq1 = HomeFaqModel.builder()
            .config(savedConfig)
            .order((short) 0)
            .question("Should be second?")
            .answer("Yes")
            .build();

        HomeFaqModel faq2 = HomeFaqModel.builder()
            .config(savedConfig)
            .order((short) 1)
            .question("Should be first?")
            .answer("Yes")
            .build();

        HomeFaqModel savedFaq1 = homeFaqRepository.save(faq1);
        HomeFaqModel savedFaq2 = homeFaqRepository.save(faq2);

        List<FrequentlyAskedQuestionUpdate> updates = List.of(
            new FrequentlyAskedQuestionUpdate(
                savedFaq2.getId(),
                "Should be first?",
                "Yes"
            ),
            new FrequentlyAskedQuestionUpdate(
                savedFaq1.getId(),
                "Should be second?",
                "Yes"
            )
        );

        // Act
        boolean result = homeFaqService.updateFaqs(savedConfig, updates);

        // Assert
        assertTrue(result);
        Optional<HomeFaqModel> updated1 = homeFaqRepository.findById(savedFaq1.getId());
        Optional<HomeFaqModel> updated2 = homeFaqRepository.findById(savedFaq2.getId());

        assertTrue(updated1.isPresent());
        assertTrue(updated2.isPresent());
        assertEquals(1, updated1.get().getOrder());
        assertEquals(0, updated2.get().getOrder());

        log.info("----------- Finalizó correctamente HomeFaqServiceIT.updateFaqs_WithOrderChange_ShouldUpdateOrder -----------");
    }

}
