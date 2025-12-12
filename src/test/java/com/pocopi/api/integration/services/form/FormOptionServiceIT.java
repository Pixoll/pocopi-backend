package com.pocopi.api.integration.services.form;

import com.pocopi.api.dto.form.FormOptionUpdate;
import com.pocopi.api.models.config.ConfigModel;
import com.pocopi.api.models.form.FormModel;
import com.pocopi.api.models.form.FormQuestionModel;
import com.pocopi.api.models.form.FormQuestionOptionModel;
import com.pocopi.api.models.form.FormQuestionType;
import com.pocopi.api.models.form.FormType;
import com.pocopi.api.repositories.ConfigRepository;
import com.pocopi.api.repositories.FormQuestionOptionRepository;
import com.pocopi.api.repositories.FormQuestionRepository;
import com.pocopi.api.repositories.FormRepository;
import com.pocopi.api.repositories.ImageRepository;
import com.pocopi.api.services.FormOptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("integration")
class FormOptionServiceIT {

    private static final Logger log = LoggerFactory.getLogger(FormOptionServiceIT.class);

    @Autowired
    private FormOptionService formOptionService;

    @Autowired
    private FormQuestionOptionRepository formQuestionOptionRepository;

    @Autowired
    private FormQuestionRepository formQuestionRepository;

    @Autowired
    private FormRepository formRepository;

    @Autowired
    private ConfigRepository configRepository;

    @Autowired
    private ImageRepository imageRepository;

    private MockMultipartFile mockPngFile;
    private FormQuestionModel testQuestion;

    @BeforeEach
    void setUp() {
        mockPngFile = new MockMultipartFile(
            "file",
            "test.png",
            "image/png",
            new byte[]{
                (byte) 0x89, (byte) 0x50, (byte) 0x4E, (byte) 0x47, (byte) 0x0D, (byte) 0x0A,
                (byte) 0x1A, (byte) 0x0A, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x0D,
                (byte) 0x49, (byte) 0x48, (byte) 0x44, (byte) 0x52, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01,
                (byte) 0x08, (byte) 0x06, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x1F,
                (byte) 0x15, (byte) 0xC4, (byte) 0x89, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x0D, (byte) 0x49, (byte) 0x44, (byte) 0x41, (byte) 0x54, (byte) 0x78,
                (byte) 0xDA, (byte) 0x62, (byte) 0x00, (byte) 0x02, (byte) 0x00, (byte) 0x00,
                (byte) 0x05, (byte) 0x00, (byte) 0x01, (byte) 0xE2, (byte) 0x26, (byte) 0x05,
                (byte) 0x5B, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x49,
                (byte) 0x45, (byte) 0x4E, (byte) 0x44, (byte) 0xAE, (byte) 0x42, (byte) 0x60,
                (byte) 0x82
            }
        );

        testQuestion = createTestQuestion();
    }

    private FormQuestionModel createTestQuestion() {
        ConfigModel config = ConfigModel.builder()
            .title("Test Config")
            .description("Test Description")
            .informedConsent("Test Consent")
            .build();
        configRepository.save(config);

        FormModel form = FormModel.builder()
            .config(config)
            .type(FormType.PRE)
            .title("Test Form")
            .build();
        formRepository.save(form);

        FormQuestionModel question = FormQuestionModel.builder()
            .form(form)
            .order((short) 0)
            .category("Test Category")
            .type(FormQuestionType.SELECT_ONE)
            .text("Test Question")
            .image(null)
            .required(true)
            .other(false)
            .build();

        return formQuestionRepository.save(question);
    }

    private List<MultipartFile> createFileListWithNulls(int size) {
        List<MultipartFile> list = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            list.add(null);
        }
        return list;
    }

    // ==================== cloneOptions Tests ====================

//    @Test
//    @Transactional
//    void cloneOptions_WithExistingOptions_ShouldCloneInDb() {
//        log.info("----------- Iniciando cloneOptions_WithExistingOptions_ShouldCloneInDb -----------");
//
//        // Arrange
//        FormQuestionModel originalQuestion = testQuestion;
//        FormQuestionModel newQuestion = createTestQuestion();
//
//        ImageModel image = ImageModel.builder()
//            .path("images/test/original.png")
//            .alt("Original Image")
//            .build();
//        imageRepository.save(image);
//
//        FormQuestionOptionModel option = FormQuestionOptionModel.builder()
//            .formQuestion(originalQuestion)
//            .text("Option 1")
//            .image(image)
//            .order((short) 0)
//            .build();
//        formQuestionOptionRepository.save(option);
//
//        // Act
//        formOptionService.cloneOptions(originalQuestion.getId(), newQuestion);
//
//        // Assert
//        List<FormQuestionOptionModel> clonedOptions = formQuestionOptionRepository
//            .findAllByFormQuestionId(newQuestion.getId());
//
//        assertEquals(1, clonedOptions.size());
//        assertEquals("Option 1", clonedOptions.get(0).getText());
//        assertNotNull(clonedOptions.get(0).getImage());
//        assertNotEquals(image.getId(), clonedOptions.get(0).getImage().getId());
//
//        log.info("----------- Finalizado cloneOptions_WithExistingOptions_ShouldCloneInDb -----------");
//    }

    @Test
    @Transactional
    void cloneOptions_WithMultipleOptions_ShouldCloneAllAndPreserveOrder() {
        log.info("----------- Iniciando cloneOptions_WithMultipleOptions_ShouldCloneAllAndPreserveOrder -----------");

        // Arrange
        FormQuestionModel originalQuestion = testQuestion;
        FormQuestionModel newQuestion = createTestQuestion();

        FormQuestionOptionModel option1 = FormQuestionOptionModel.builder()
            .formQuestion(originalQuestion)
            .text("Option A")
            .order((short) 0)
            .build();
        formQuestionOptionRepository.save(option1);

        FormQuestionOptionModel option2 = FormQuestionOptionModel.builder()
            .formQuestion(originalQuestion)
            .text("Option B")
            .order((short) 1)
            .build();
        formQuestionOptionRepository.save(option2);

        // Act
        formOptionService.cloneOptions(originalQuestion.getId(), newQuestion);

        // Assert
        List<FormQuestionOptionModel> clonedOptions = formQuestionOptionRepository
            .findAllByFormQuestionId(newQuestion.getId());

        assertEquals(2, clonedOptions.size());
        assertEquals("Option A", clonedOptions.get(0).getText());
        assertEquals(0, clonedOptions.get(0).getOrder());
        assertEquals("Option B", clonedOptions.get(1).getText());
        assertEquals(1, clonedOptions.get(1).getOrder());

        log.info("----------- Finalizado cloneOptions_WithMultipleOptions_ShouldCloneAllAndPreserveOrder -----------");
    }

    @Test
    @Transactional
    void cloneOptions_WithOptionWithoutImage_ShouldCloneWithoutImage() {
        log.info("----------- Iniciando cloneOptions_WithOptionWithoutImage_ShouldCloneWithoutImage -----------");

        // Arrange
        FormQuestionModel originalQuestion = testQuestion;
        FormQuestionModel newQuestion = createTestQuestion();

        FormQuestionOptionModel originalOption = FormQuestionOptionModel.builder()
            .formQuestion(originalQuestion)
            .text("Option")
            .image(null)
            .order((short) 0)
            .build();
        formQuestionOptionRepository.save(originalOption);

        // Act
        formOptionService.cloneOptions(originalQuestion.getId(), newQuestion);

        // Assert
        List<FormQuestionOptionModel> clonedOptions = formQuestionOptionRepository
            .findAllByFormQuestionId(newQuestion.getId());

        assertEquals(1, clonedOptions.size());
        assertNull(clonedOptions.get(0).getImage());

        log.info("----------- Finalizado cloneOptions_WithOptionWithoutImage_ShouldCloneWithoutImage -----------");
    }

    // ==================== updateOptions Tests ====================

    @Test
    @Transactional
    void updateOptions_WithNewOption_WithoutImage_ShouldCreateInDb() {
        log.info("----------- Iniciando updateOptions_WithNewOption_WithoutImage_ShouldCreateInDb -----------");

        // Arrange
        FormOptionUpdate newOption = new FormOptionUpdate(null, "New Option");

        Map<Integer, FormQuestionOptionModel> storedMap = new HashMap<>();
        Map<Integer, Boolean> processedMap = new HashMap<>();
        AtomicInteger imageIndex = new AtomicInteger(0);
        List<MultipartFile> files = createFileListWithNulls(1);

        // Act
        boolean result = formOptionService.updateOptions(
            testQuestion, List.of(newOption), storedMap, processedMap, imageIndex, files
        );

        // Assert
        assertTrue(result);
        List<FormQuestionOptionModel> options = formQuestionOptionRepository
            .findAllByFormQuestionId(testQuestion.getId());

        assertEquals(1, options.size());
        assertEquals("New Option", options.get(0).getText());
        assertNull(options.get(0).getImage());

        log.info("----------- Finalizado updateOptions_WithNewOption_WithoutImage_ShouldCreateInDb -----------");
    }

    @Test
    @Transactional
    void updateOptions_WithNewOption_WithImage_ShouldCreateWithImage() {
        log.info("----------- Iniciando updateOptions_WithNewOption_WithImage_ShouldCreateWithImage -----------");

        // Arrange
        FormOptionUpdate newOption = new FormOptionUpdate(null, "Option with Image");

        Map<Integer, FormQuestionOptionModel> storedMap = new HashMap<>();
        Map<Integer, Boolean> processedMap = new HashMap<>();
        AtomicInteger imageIndex = new AtomicInteger(0);
        List<MultipartFile> files = List.of(mockPngFile);

        // Act
        boolean result = formOptionService.updateOptions(
            testQuestion, List.of(newOption), storedMap, processedMap, imageIndex, files
        );

        // Assert
        assertTrue(result);
        List<FormQuestionOptionModel> options = formQuestionOptionRepository
            .findAllByFormQuestionId(testQuestion.getId());

        assertEquals(1, options.size());
        assertNotNull(options.get(0).getImage());
        assertTrue(options.get(0).getImage().getPath().contains("test.png"));

        log.info("----------- Finalizado updateOptions_WithNewOption_WithImage_ShouldCreateWithImage -----------");
    }

    @Test
    @Transactional
    void updateOptions_WithExistingOption_ChangeText_ShouldUpdateInDb() {
        log.info("----------- Iniciando updateOptions_WithExistingOption_ChangeText_ShouldUpdateInDb -----------");

        // Arrange
        FormQuestionOptionModel storedOption = FormQuestionOptionModel.builder()
            .formQuestion(testQuestion)
            .text("Old Text")
            .order((short) 0)
            .build();
        formQuestionOptionRepository.save(storedOption);

        FormOptionUpdate update = new FormOptionUpdate(storedOption.getId(), "New Text");

        Map<Integer, FormQuestionOptionModel> storedMap = new HashMap<>();
        storedMap.put(storedOption.getId(), storedOption);
        Map<Integer, Boolean> processedMap = new HashMap<>();
        AtomicInteger imageIndex = new AtomicInteger(0);
        List<MultipartFile> files = createFileListWithNulls(1);

        // Act
        boolean result = formOptionService.updateOptions(
            testQuestion, List.of(update), storedMap, processedMap, imageIndex, files
        );

        // Assert
        assertTrue(result);
        Optional<FormQuestionOptionModel> found = formQuestionOptionRepository.findById(storedOption.getId());
        assertTrue(found.isPresent());
        assertEquals("New Text", found.get().getText());

        log.info("----------- Finalizado updateOptions_WithExistingOption_ChangeText_ShouldUpdateInDb -----------");
    }

    @Test
    @Transactional
    void updateOptions_WithExistingOption_NoChanges_ShouldReturnFalse() {
        log.info("----------- Iniciando updateOptions_WithExistingOption_NoChanges_ShouldReturnFalse -----------");

        // Arrange
        FormQuestionOptionModel storedOption = FormQuestionOptionModel.builder()
            .formQuestion(testQuestion)
            .text("Option")
            .order((short) 0)
            .build();
        formQuestionOptionRepository.save(storedOption);

        FormOptionUpdate update = new FormOptionUpdate(storedOption.getId(), "Option");

        Map<Integer, FormQuestionOptionModel> storedMap = new HashMap<>();
        storedMap.put(storedOption.getId(), storedOption);
        Map<Integer, Boolean> processedMap = new HashMap<>();
        AtomicInteger imageIndex = new AtomicInteger(0);
        List<MultipartFile> files = createFileListWithNulls(1);

        // Act
        boolean result = formOptionService.updateOptions(
            testQuestion, List.of(update), storedMap, processedMap, imageIndex, files
        );

        // Assert
        assertFalse(result);
        assertTrue(processedMap.get(storedOption.getId()));

        log.info("----------- Finalizado updateOptions_WithExistingOption_NoChanges_ShouldReturnFalse -----------");
    }

    @Test
    @Transactional
    void updateOptions_WithExistingOption_ChangedOrder_ShouldUpdate() {
        log.info("----------- Iniciando updateOptions_WithExistingOption_ChangedOrder_ShouldUpdate -----------");

        // Arrange
        FormQuestionOptionModel storedOption = FormQuestionOptionModel.builder()
            .formQuestion(testQuestion)
            .text("Option")
            .order((short) 0)
            .build();
        formQuestionOptionRepository.save(storedOption);

        FormOptionUpdate update = new FormOptionUpdate(storedOption.getId(), "Option");

        Map<Integer, FormQuestionOptionModel> storedMap = new HashMap<>();
        storedMap.put(storedOption.getId(), storedOption);
        Map<Integer, Boolean> processedMap = new HashMap<>();
        AtomicInteger imageIndex = new AtomicInteger(0);
        List<MultipartFile> files = createFileListWithNulls(2);

        // Act
        boolean result = formOptionService.updateOptions(
            testQuestion, List.of(new FormOptionUpdate(null, "First"), update),
            storedMap, processedMap, imageIndex, files
        );

        // Assert
        assertTrue(result);
        assertEquals(1, storedOption.getOrder());

        log.info("----------- Finalizado updateOptions_WithExistingOption_ChangedOrder_ShouldUpdate -----------");
    }

    @Test
    @Transactional
    void updateOptions_WithExistingOption_AddImage_ShouldUpdateInDb() {
        log.info("----------- Iniciando updateOptions_WithExistingOption_AddImage_ShouldUpdateInDb -----------");

        // Arrange
        FormQuestionOptionModel storedOption = FormQuestionOptionModel.builder()
            .formQuestion(testQuestion)
            .text("Option")
            .order((short) 0)
            .image(null)
            .build();
        formQuestionOptionRepository.save(storedOption);

        FormOptionUpdate update = new FormOptionUpdate(storedOption.getId(), "Option");

        Map<Integer, FormQuestionOptionModel> storedMap = new HashMap<>();
        storedMap.put(storedOption.getId(), storedOption);
        Map<Integer, Boolean> processedMap = new HashMap<>();
        AtomicInteger imageIndex = new AtomicInteger(0);
        List<MultipartFile> files = List.of(mockPngFile);

        // Act
        boolean result = formOptionService.updateOptions(
            testQuestion, List.of(update), storedMap, processedMap, imageIndex, files
        );

        // Assert
        assertTrue(result);
        Optional<FormQuestionOptionModel> found = formQuestionOptionRepository.findById(storedOption.getId());
        assertTrue(found.isPresent());
        assertNotNull(found.get().getImage());

        log.info("----------- Finalizado updateOptions_WithExistingOption_AddImage_ShouldUpdateInDb -----------");
    }

    @Test
    @Transactional
    void updateOptions_WithMultipleOptions_MixedOperations_ShouldHandleCorrectly() {
        log.info("----------- Iniciando updateOptions_WithMultipleOptions_MixedOperations_ShouldHandleCorrectly -----------");

        // Arrange
        FormQuestionOptionModel existing = FormQuestionOptionModel.builder()
            .formQuestion(testQuestion)
            .text("Existing")
            .order((short) 0)
            .build();
        formQuestionOptionRepository.save(existing);

        FormOptionUpdate existingUpdate = new FormOptionUpdate(existing.getId(), "Updated");
        FormOptionUpdate newOption1 = new FormOptionUpdate(null, "New 1");
        FormOptionUpdate newOption2 = new FormOptionUpdate(null, "New 2");

        Map<Integer, FormQuestionOptionModel> storedMap = new HashMap<>();
        storedMap.put(existing.getId(), existing);
        Map<Integer, Boolean> processedMap = new HashMap<>();
        AtomicInteger imageIndex = new AtomicInteger(0);
        List<MultipartFile> files = createFileListWithNulls(3);

        // Act
        boolean result = formOptionService.updateOptions(
            testQuestion,
            List.of(existingUpdate, newOption1, newOption2),
            storedMap, processedMap, imageIndex, files
        );

        // Assert
        assertTrue(result);
        List<FormQuestionOptionModel> allOptions = formQuestionOptionRepository
            .findAllByFormQuestionId(testQuestion.getId());

        assertEquals(3, allOptions.size());
        assertEquals("Updated", allOptions.get(0).getText());
        assertEquals("New 1", allOptions.get(1).getText());
        assertEquals("New 2", allOptions.get(2).getText());

        log.info("----------- Finalizado updateOptions_WithMultipleOptions_MixedOperations_ShouldHandleCorrectly -----------");
    }
}
