package com.pocopi.api.integration.services.form;

import com.pocopi.api.dto.form.FormQuestionUpdate;
import com.pocopi.api.models.config.ConfigModel;
import com.pocopi.api.models.form.*;
import com.pocopi.api.repositories.*;
import com.pocopi.api.services.FormQuestionService;
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

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
@ActiveProfiles("integration")
class FormQuestionServiceIT {

    private static final Logger log = LoggerFactory.getLogger(FormQuestionServiceIT.class);

    @Autowired
    private FormQuestionService formQuestionService;

    @Autowired
    private FormQuestionRepository formQuestionRepository;

    @Autowired
    private FormRepository formRepository;

    @Autowired
    private ConfigRepository configRepository;

    private ConfigModel testConfig;
    private FormModel testForm;

    @BeforeEach
    void setUp() {
        testConfig = ConfigModel.builder()
            .title("Test Config")
            .subtitle("Test Subtitle")
            .description("Test Description")
            .informedConsent("Test Informed Consent")
            .anonymous(true)
            .build();
        testConfig = configRepository.save(testConfig);

        testForm = FormModel.builder()
            .config(testConfig)
            .title("Test Form")
            .type(FormType.PRE)
            .build();
        testForm = formRepository.save(testForm);
    }

    // ==================== cloneQuestions Tests ====================

    @Test
    @Transactional
    void cloneQuestions_WithSingleQuestion_ShouldCloneSuccessfully() {
        log.info("----------- Iniciando cloneQuestions_WithSingleQuestion_ShouldCloneSuccessfully -----------");

        // Arrange
        FormQuestionModel originalQuestion = FormQuestionModel.builder()
            .form(testForm)
            .order((short) 0)
            .category("personal_info")
            .text("What is your name?")
            .type(FormQuestionType.TEXT_SHORT)
            .minLength(1)
            .maxLength(100)
            .placeholder("Enter name")
            .required(true)
            .build();
        originalQuestion = formQuestionRepository.save(originalQuestion);

        FormModel newForm = FormModel.builder()
            .config(testConfig)
            .title("New Form")
            .type(FormType.POST)
            .build();
        newForm = formRepository.save(newForm);

        long originalCount = formQuestionRepository.count();

        // Act
        formQuestionService.cloneQuestions(testForm.getId(), newForm);

        // Assert
        long newCount = formQuestionRepository.count();
        assertEquals(originalCount + 1, newCount);

        List<FormQuestionModel> clonedQuestions = formQuestionRepository.findAllByFormId(newForm.getId());
        assertEquals(1, clonedQuestions.size());

        FormQuestionModel cloned = clonedQuestions.get(0);
        assertEquals(originalQuestion.getCategory(), cloned.getCategory());
        assertEquals(originalQuestion.getText(), cloned.getText());
        assertEquals(originalQuestion.getType(), cloned.getType());
        assertNull(cloned.getImage());

        log.info("----------- Finalizó correctamente cloneQuestions_WithSingleQuestion_ShouldCloneSuccessfully -----------");
    }

    @Test
    @Transactional
    void cloneQuestions_WithMultipleQuestions_ShouldPreserveOrder() {
        log.info("----------- Iniciando cloneQuestions_WithMultipleQuestions_ShouldPreserveOrder -----------");

        // Arrange
        FormQuestionModel q1 = FormQuestionModel.builder()
            .form(testForm)
            .order((short) 0)
            .category("cat1")
            .text("Question 1")
            .type(FormQuestionType.TEXT_SHORT)
            .minLength(1)
            .maxLength(50)
            .placeholder("Q1")
            .required(true)
            .build();
        q1 = formQuestionRepository.save(q1);

        FormQuestionModel q2 = FormQuestionModel.builder()
            .form(testForm)
            .order((short) 1)
            .category("cat2")
            .text("Question 2")
            .type(FormQuestionType.TEXT_LONG)
            .minLength(1)
            .maxLength(500)
            .placeholder("Q2")
            .required(true)
            .build();
        q2 = formQuestionRepository.save(q2);

        FormModel newForm = FormModel.builder()
            .config(testConfig)
            .title("Multi Question Form")
            .type(FormType.POST)
            .build();
        newForm = formRepository.save(newForm);

        // Act
        formQuestionService.cloneQuestions(testForm.getId(), newForm);

        // Assert
        List<FormQuestionModel> clonedQuestions = formQuestionRepository.findAllByFormIdOrderByOrder(newForm.getId());
        assertEquals(2, clonedQuestions.size());
        assertEquals((short) 0, clonedQuestions.get(0).getOrder());
        assertEquals((short) 1, clonedQuestions.get(1).getOrder());
        assertEquals("Question 1", clonedQuestions.get(0).getText());
        assertEquals("Question 2", clonedQuestions.get(1).getText());

        log.info("----------- Finalizó correctamente cloneQuestions_WithMultipleQuestions_ShouldPreserveOrder -----------");
    }


    // ==================== updateQuestions Tests ====================

    @Test
    @Transactional
    void updateQuestions_CreateTextShortQuestion_ShouldSucceed() {
        // Arrange
        FormQuestionUpdate.TextShortUpdate update = new FormQuestionUpdate.TextShortUpdate(
            null, "contact", "Enter your email", FormQuestionType.TEXT_SHORT,
            "example@email.com", 5, 100
        );

        long originalCount = formQuestionRepository.count();

        // Act
        boolean modified = formQuestionService.updateQuestions(
            testForm, List.of(update),
            new HashMap<>(), new HashMap<>(), new HashMap<>(),
            new HashMap<>(), new HashMap<>(), new HashMap<>(),
            new AtomicInteger(0), Collections.singletonList((MultipartFile) null)
        );

        // Assert
        assertTrue(modified);
        assertEquals(originalCount + 1, formQuestionRepository.count());

        List<FormQuestionModel> questions = formQuestionRepository.findAllByFormIdOrderByOrder(testForm.getId());
        FormQuestionModel created = questions.get(0);
        assertEquals(FormQuestionType.TEXT_SHORT, created.getType());
        assertEquals("example@email.com", created.getPlaceholder());
        assertEquals(5, created.getMinLength());
        assertEquals(100, created.getMaxLength());
    }

    @Test
    @Transactional
    void updateQuestions_CreateSelectOneQuestion_ShouldSucceed() {
        log.info("----------- Iniciando updateQuestions_CreateSelectOneQuestion_ShouldSucceed -----------");

        // Arrange
        FormQuestionUpdate.SelectOneUpdate update = new FormQuestionUpdate.SelectOneUpdate(
            null, "choice", "Pick your favorite", FormQuestionType.SELECT_ONE, false, Collections.emptyList()
        );

        List<MultipartFile> imageFiles = new ArrayList<>();
        imageFiles.add(null);

        // Act
        boolean modified = formQuestionService.updateQuestions(
            testForm, List.of(update),
            new HashMap<>(), new HashMap<>(), new HashMap<>(),
            new HashMap<>(), new HashMap<>(), new HashMap<>(),
            new AtomicInteger(0), imageFiles
        );

        // Assert
        assertTrue(modified);

        List<FormQuestionModel> questions = formQuestionRepository.findAllByFormId(testForm.getId());
        FormQuestionModel created = questions.get(0);
        assertEquals(FormQuestionType.SELECT_ONE, created.getType());
        assertFalse(created.getOther());

        log.info("----------- Finalizó correctamente updateQuestions_CreateSelectOneQuestion_ShouldSucceed -----------");
    }

    @Test
    @Transactional
    void updateQuestions_CreateSelectMultipleQuestion_ShouldSucceed() {
        log.info("----------- Iniciando updateQuestions_CreateSelectMultipleQuestion_ShouldSucceed -----------");

        // Arrange
        FormQuestionUpdate.SelectMultipleUpdate update = new FormQuestionUpdate.SelectMultipleUpdate(
            null, "demographics", "Select all that apply", FormQuestionType.SELECT_MULTIPLE,
            2, 5, true, Collections.emptyList()
        );

        List<MultipartFile> imageFiles = new ArrayList<>();
        imageFiles.add(null);

        // Act
        boolean modified = formQuestionService.updateQuestions(
            testForm, List.of(update),
            new HashMap<>(), new HashMap<>(), new HashMap<>(),
            new HashMap<>(), new HashMap<>(), new HashMap<>(),
            new AtomicInteger(0), imageFiles
        );

        // Assert
        assertTrue(modified);

        List<FormQuestionModel> questions = formQuestionRepository.findAllByFormId(testForm.getId());
        FormQuestionModel created = questions.get(0);
        assertEquals(FormQuestionType.SELECT_MULTIPLE, created.getType());
        assertEquals(2, created.getMin());
        assertEquals(5, created.getMax());
        assertTrue(created.getOther());

        log.info("----------- Finalizó correctamente updateQuestions_CreateSelectMultipleQuestion_ShouldSucceed -----------");
    }

    @Test
    @Transactional
    void updateQuestions_CreateSliderQuestion_ShouldSucceed() {
        log.info("----------- Iniciando updateQuestions_CreateSliderQuestion_ShouldSucceed -----------");

        // Arrange
        FormQuestionUpdate.SliderUpdate update = new FormQuestionUpdate.SliderUpdate(
            null, "satisfaction", "Rate your experience", FormQuestionType.SLIDER,
            0, 100, 10, Collections.emptyList()
        );

        List<MultipartFile> imageFiles = new ArrayList<>();
        imageFiles.add(null);

        // Act
        boolean modified = formQuestionService.updateQuestions(
            testForm, List.of(update),
            new HashMap<>(), new HashMap<>(), new HashMap<>(),
            new HashMap<>(), new HashMap<>(), new HashMap<>(),
            new AtomicInteger(0), imageFiles
        );

        // Assert
        assertTrue(modified);

        List<FormQuestionModel> questions = formQuestionRepository.findAllByFormId(testForm.getId());
        FormQuestionModel created = questions.get(0);
        assertEquals(FormQuestionType.SLIDER, created.getType());
        assertEquals(0, created.getMin());
        assertEquals(100, created.getMax());
        assertEquals(10, created.getStep());

        log.info("----------- Finalizó correctamente updateQuestions_CreateSliderQuestion_ShouldSucceed -----------");
    }

    @Test
    @Transactional
    void updateQuestions_UpdateExistingQuestion_ShouldPreserveId() {
        log.info("----------- Iniciando updateQuestions_UpdateExistingQuestion_ShouldPreserveId -----------");

        // Arrange
        FormQuestionModel original = FormQuestionModel.builder()
            .form(testForm)
            .order((short) 0)
            .category("old_category")
            .text("Old text")
            .type(FormQuestionType.TEXT_SHORT)
            .minLength(1)
            .maxLength(50)
            .placeholder("old")
            .required(true)
            .build();
        original = formQuestionRepository.save(original);

        FormQuestionUpdate.TextShortUpdate update = new FormQuestionUpdate.TextShortUpdate(
            original.getId(), "new_category", "New text", FormQuestionType.TEXT_SHORT,
            "new", 5, 100
        );

        List<MultipartFile> imageFiles = new ArrayList<>();
        imageFiles.add(null);

        // Act
        boolean modified = formQuestionService.updateQuestions(
            testForm, List.of(update),
            Map.of(original.getId(), original), new HashMap<>(), new HashMap<>(),
            new HashMap<>(), new HashMap<>(), new HashMap<>(),
            new AtomicInteger(0), imageFiles
        );

        // Assert
        assertTrue(modified);

        FormQuestionModel updated = formQuestionRepository.findById(original.getId()).orElseThrow();
        assertEquals(original.getId(), updated.getId());
        assertEquals("new_category", updated.getCategory());
        assertEquals("New text", updated.getText());
        assertEquals("new", updated.getPlaceholder());

        log.info("----------- Finalizó correctamente updateQuestions_UpdateExistingQuestion_ShouldPreserveId -----------");
    }

    @Test
    @Transactional
    void updateQuestions_MultipleQuestions_ShouldUpdateOrderCorrectly() {
        log.info("----------- Iniciando updateQuestions_MultipleQuestions_ShouldUpdateOrderCorrectly -----------");

        // Arrange
        FormQuestionModel q1 = FormQuestionModel.builder()
            .form(testForm)
            .order((short) 0)
            .category("cat1")
            .text("Q1")
            .type(FormQuestionType.TEXT_SHORT)
            .minLength(1)
            .maxLength(50)
            .placeholder("Q1")
            .required(true)
            .build();
        q1 = formQuestionRepository.save(q1);

        FormQuestionModel q2 = FormQuestionModel.builder()
            .form(testForm)
            .order((short) 1)
            .category("cat2")
            .text("Q2")
            .type(FormQuestionType.TEXT_SHORT)
            .minLength(1)
            .maxLength(50)
            .placeholder("Q2")
            .required(true)
            .build();
        q2 = formQuestionRepository.save(q2);

        FormQuestionUpdate.TextShortUpdate update1 = new FormQuestionUpdate.TextShortUpdate(
            q1.getId(), "cat1_updated", "Q1 Updated", FormQuestionType.TEXT_SHORT, "q1", 1, 50
        );

        FormQuestionUpdate.TextShortUpdate newQuestion = new FormQuestionUpdate.TextShortUpdate(
            null, "cat_new", "Q New", FormQuestionType.TEXT_SHORT, "new", 1, 50
        );

        FormQuestionUpdate.TextShortUpdate update2 = new FormQuestionUpdate.TextShortUpdate(
            q2.getId(), "cat2_updated", "Q2 Updated", FormQuestionType.TEXT_SHORT, "q2", 1, 50
        );

        List<MultipartFile> imageFiles = new ArrayList<>();
        imageFiles.add(null);
        imageFiles.add(null);
        imageFiles.add(null);

        // Act
        boolean modified = formQuestionService.updateQuestions(
            testForm, List.of(update1, newQuestion, update2),
            Map.of(q1.getId(), q1, q2.getId(), q2), new HashMap<>(), new HashMap<>(),
            new HashMap<>(), new HashMap<>(), new HashMap<>(),
            new AtomicInteger(0), imageFiles
        );

        // Assert
        assertTrue(modified);

        List<FormQuestionModel> questions = formQuestionRepository.findAllByFormIdOrderByOrder(testForm.getId());
        assertEquals(3, questions.size());
        assertEquals((short) 0, questions.get(0).getOrder());
        assertEquals((short) 1, questions.get(1).getOrder());
        assertEquals((short) 2, questions.get(2).getOrder());

        log.info("----------- Finalizó correctamente updateQuestions_MultipleQuestions_ShouldUpdateOrderCorrectly -----------");
    }

    @Test
    @Transactional
    void updateQuestions_CreateTextLongQuestion_ShouldSucceed() {
        log.info("----------- Iniciando updateQuestions_CreateTextLongQuestion_ShouldSucceed -----------");

        // Arrange
        FormQuestionUpdate.TextLongUpdate update = new FormQuestionUpdate.TextLongUpdate(
            null, "feedback", "Tell us your feedback", FormQuestionType.TEXT_LONG,
            "Enter feedback...", 10, 500
        );

        List<MultipartFile> imageFiles = new ArrayList<>();
        imageFiles.add(null);

        // Act
        boolean modified = formQuestionService.updateQuestions(
            testForm, List.of(update),
            new HashMap<>(), new HashMap<>(), new HashMap<>(),
            new HashMap<>(), new HashMap<>(), new HashMap<>(),
            new AtomicInteger(0), imageFiles
        );

        // Assert
        assertTrue(modified);

        List<FormQuestionModel> questions = formQuestionRepository.findAllByFormId(testForm.getId());
        FormQuestionModel created = questions.get(0);
        assertEquals(FormQuestionType.TEXT_LONG, created.getType());
        assertEquals("Enter feedback...", created.getPlaceholder());
        assertEquals(10, created.getMinLength());
        assertEquals(500, created.getMaxLength());

        log.info("----------- Finalizó correctamente updateQuestions_CreateTextLongQuestion_ShouldSucceed -----------");
    }


    @Test
    @Transactional
    void updateQuestions_NoChanges_ShouldReturnFalse() {
        log.info("----------- Iniciando updateQuestions_NoChanges_ShouldReturnFalse -----------");

        // Arrange
        FormQuestionModel existing = FormQuestionModel.builder()
            .form(testForm)
            .order((short) 0)
            .category("cat")
            .text("Text")
            .type(FormQuestionType.TEXT_SHORT)
            .minLength(1)
            .maxLength(50)
            .placeholder("ph")
            .required(true)
            .build();
        existing = formQuestionRepository.save(existing);

        FormQuestionUpdate.TextShortUpdate update = new FormQuestionUpdate.TextShortUpdate(
            existing.getId(), "cat", "Text", FormQuestionType.TEXT_SHORT, "ph", 1, 50
        );

        List<MultipartFile> imageFiles = new ArrayList<>();
        imageFiles.add(null);

        // Act
        boolean modified = formQuestionService.updateQuestions(
            testForm, List.of(update),
            Map.of(existing.getId(), existing), new HashMap<>(), new HashMap<>(),
            new HashMap<>(), new HashMap<>(), new HashMap<>(),
            new AtomicInteger(0), imageFiles
        );

        // Assert
        assertFalse(modified, "No debe indicar que fue modificado cuando no hay cambios");

        log.info("----------- Finalizó correctamente updateQuestions_NoChanges_ShouldReturnFalse -----------");
    }


    @Test
    @Transactional
    void updateQuestions_NullList_ShouldReturnTrue() {
        log.info("----------- Iniciando updateQuestions_NullList_ShouldReturnTrue -----------");

        // Act
        boolean modified = formQuestionService.updateQuestions(
            testForm, null,
            new HashMap<>(), new HashMap<>(), new HashMap<>(),
            new HashMap<>(), new HashMap<>(), new HashMap<>(),
            new AtomicInteger(0), Collections.emptyList()
        );

        // Assert
        assertTrue(modified, "Debe retornar true para lista nula");

        log.info("----------- Finalizó correctamente updateQuestions_NullList_ShouldReturnTrue -----------");
    }
}