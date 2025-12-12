package com.pocopi.api.integration.services.form;

import com.pocopi.api.dto.form.*;
import com.pocopi.api.models.config.ConfigModel;
import com.pocopi.api.models.form.*;
import com.pocopi.api.repositories.*;
import com.pocopi.api.services.FormService;
import com.pocopi.api.services.ImageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("integration")
@DisplayName("FormService Integration Tests")
class FormServiceIT {

    private static final Logger log = LoggerFactory.getLogger(FormServiceIT.class);

    @Autowired
    private FormService formService;

    @Autowired
    private FormRepository formRepository;

    @Autowired
    private FormQuestionRepository formQuestionRepository;

    @Autowired
    private FormQuestionOptionRepository formQuestionOptionRepository;

    @Autowired
    private FormQuestionSliderLabelRepository formQuestionSliderLabelRepository;

    @Autowired
    private ConfigRepository configRepository;

    @Autowired
    private ImageService imageService;

    private ConfigModel testConfig;
    private MockMultipartFile mockPngFile;

    @BeforeEach
    void setUp() {
        testConfig = ConfigModel.builder()
            .title("Test Config")
            .description("Test Description")
            .informedConsent("Test Consent")
            .build();
        configRepository.save(testConfig);

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
    }

    // ==================== getFormsByConfigVersion Tests ====================

    @Test
    @Transactional
    @DisplayName("Should retrieve forms with select-one questions and options")
    void getFormsByConfigVersion_WithSelectOneQuestions_ShouldReturnFormsWithOptions() {
        log.info("----------- Test: getFormsByConfigVersion with SELECT_ONE questions -----------");

        // Arrange
        FormModel preForm = FormModel.builder()
            .config(testConfig)
            .type(FormType.PRE)
            .title("Pre Form")
            .build();
        formRepository.save(preForm);

        FormQuestionModel question = FormQuestionModel.builder()
            .form(preForm)
            .order((short) 0)
            .category("Demographics")
            .text("What is your gender?")
            .type(FormQuestionType.SELECT_ONE)
            .required(true)
            .other(false)
            .build();
        formQuestionRepository.save(question);

        FormQuestionOptionModel option1 = FormQuestionOptionModel.builder()
            .formQuestion(question)
            .order((short) 0)
            .text("Male")
            .image(null)
            .build();
        formQuestionOptionRepository.save(option1);

        FormQuestionOptionModel option2 = FormQuestionOptionModel.builder()
            .formQuestion(question)
            .order((short) 1)
            .text("Female")
            .image(null)
            .build();
        formQuestionOptionRepository.save(option2);

        // Act
        Map<FormType, Form> result = formService.getFormsByConfigVersion(testConfig.getVersion());

        // Assert
        assertNotNull(result);
        assertTrue(result.containsKey(FormType.PRE));
        Form preFormResult = result.get(FormType.PRE);
        assertEquals(1, preFormResult.questions().size());

        FormQuestion formQuestion = preFormResult.questions().get(0);
        assertInstanceOf(FormQuestion.SelectOne.class, formQuestion);

        FormQuestion.SelectOne selectOne = (FormQuestion.SelectOne) formQuestion;
        assertEquals("What is your gender?", selectOne.text);
        assertEquals(2, selectOne.options.size());
        assertFalse(selectOne.other);

        log.info("----------- Test passed: SELECT_ONE questions retrieved correctly -----------");
    }

    @Test
    @Transactional
    @DisplayName("Should retrieve forms with slider questions and labels")
    void getFormsByConfigVersion_WithSliderQuestions_ShouldReturnSliderLabels() {
        log.info("----------- Test: getFormsByConfigVersion with SLIDER questions -----------");

        // Arrange
        FormModel postForm = FormModel.builder()
            .config(testConfig)
            .type(FormType.POST)
            .title("Post Form")
            .build();
        formRepository.save(postForm);

        FormQuestionModel sliderQuestion = FormQuestionModel.builder()
            .form(postForm)
            .order((short) 0)
            .category("Satisfaction")
            .text("How satisfied are you?")
            .type(FormQuestionType.SLIDER)
            .required(true)
            .min(1)
            .max(10)
            .step(1)
            .other(null)
            .build();
        formQuestionRepository.save(sliderQuestion);

        FormQuestionSliderLabelModel label1 = FormQuestionSliderLabelModel.builder()
            .formQuestion(sliderQuestion)
            .number(1)
            .label("Very Unsatisfied")
            .build();
        formQuestionSliderLabelRepository.save(label1);

        FormQuestionSliderLabelModel label10 = FormQuestionSliderLabelModel.builder()
            .formQuestion(sliderQuestion)
            .number(10)
            .label("Very Satisfied")
            .build();
        formQuestionSliderLabelRepository.save(label10);

        // Act
        Map<FormType, Form> result = formService.getFormsByConfigVersion(testConfig.getVersion());

        // Assert
        assertNotNull(result);
        assertTrue(result.containsKey(FormType.POST));
        Form postFormResult = result.get(FormType.POST);
        assertEquals(1, postFormResult.questions().size());

        FormQuestion formQuestion = postFormResult.questions().get(0);
        assertInstanceOf(FormQuestion.Slider.class, formQuestion);

        FormQuestion.Slider slider = (FormQuestion.Slider) formQuestion;
        assertEquals(1, slider.min);
        assertEquals(10, slider.max);
        assertEquals(1, slider.step);
        assertEquals(2, slider.labels.size());

        log.info("----------- Test passed: SLIDER questions with labels retrieved correctly -----------");
    }

    @Test
    @Transactional
    @DisplayName("Should retrieve forms with text questions")
    void getFormsByConfigVersion_WithTextQuestions_ShouldReturnTextProperties() {
        log.info("----------- Test: getFormsByConfigVersion with TEXT questions -----------");

        // Arrange
        FormModel preForm = FormModel.builder()
            .config(testConfig)
            .type(FormType.PRE)
            .title("Text Form")
            .build();
        formRepository.save(preForm);

        FormQuestionModel textQuestion = FormQuestionModel.builder()
            .form(preForm)
            .order((short) 0)
            .category("Personal")
            .text("What is your name?")
            .type(FormQuestionType.TEXT_SHORT)
            .required(true)
            .minLength(1)
            .maxLength(50)
            .placeholder("Enter your name")
            .other(null)
            .build();
        formQuestionRepository.save(textQuestion);

        // Act
        Map<FormType, Form> result = formService.getFormsByConfigVersion(testConfig.getVersion());

        // Assert
        assertNotNull(result);
        Form preFormResult = result.get(FormType.PRE);
        FormQuestion question = preFormResult.questions().getFirst();

        assertInstanceOf(FormQuestion.TextShort.class, question);
        FormQuestion.TextShort textShort = (FormQuestion.TextShort) question;
        assertEquals(1, textShort.minLength);
        assertEquals(50, textShort.maxLength);
        assertEquals("Enter your name", textShort.placeholder);

        log.info("----------- Test passed: TEXT questions retrieved correctly -----------");
    }

    @Test
    @Transactional
    @DisplayName("Should return empty map when no forms exist")
    void getFormsByConfigVersion_WithNoForms_ShouldReturnEmptyMap() {
        log.info("----------- Test: getFormsByConfigVersion with no forms -----------");

        // Act
        Map<FormType, Form> result = formService.getFormsByConfigVersion(testConfig.getVersion());

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());

        log.info("----------- Test passed: Empty map returned correctly -----------");
    }

    // ==================== cloneForms Tests ====================

    @Test
    @Transactional
    @DisplayName("Should clone forms with all questions and options")
    void cloneForms_WithCompleteForm_ShouldCloneAllData() {
        log.info("----------- Test: cloneForms with complete form -----------");

        // Arrange
        FormModel originalForm = FormModel.builder()
            .config(testConfig)
            .type(FormType.PRE)
            .title("Original Form")
            .build();
        formRepository.save(originalForm);

        FormQuestionModel question = FormQuestionModel.builder()
            .form(originalForm)
            .order((short) 0)
            .category("Test Category")
            .text("Test Question")
            .type(FormQuestionType.SELECT_ONE)
            .required(true)
            .other(false)
            .build();
        formQuestionRepository.save(question);

        FormQuestionOptionModel option = FormQuestionOptionModel.builder()
            .formQuestion(question)
            .order((short) 0)
            .text("Test Option")
            .image(null)
            .build();
        formQuestionOptionRepository.save(option);

        ConfigModel newConfig = ConfigModel.builder()
            .title("New Config")
            .description("New Description")
            .informedConsent("New Consent")
            .build();
        configRepository.save(newConfig);

        // Act
        formService.cloneForms(testConfig.getVersion(), newConfig);

        // Assert
        List<FormModel> clonedForms = formRepository.findAllByConfigVersion(newConfig.getVersion());
        assertEquals(1, clonedForms.size());

        FormModel clonedForm = clonedForms.getFirst();
        assertEquals(FormType.PRE, clonedForm.getType());
        assertEquals("Original Form", clonedForm.getTitle());
        assertNotEquals(originalForm.getId(), clonedForm.getId());

        List<FormQuestionModel> clonedQuestions = formQuestionRepository
            .findAllByFormIdOrderByOrder(clonedForm.getId());
        assertEquals(1, clonedQuestions.size());

        log.info("----------- Test passed: Forms cloned successfully -----------");
    }

    // ==================== updateForm Tests ====================

    @Test
    @Transactional
    @DisplayName("Should create new form when it doesn't exist")
    void updateForm_WithNewForm_ShouldCreateForm() {
        log.info("----------- Test: updateForm creating new form -----------");

        // Arrange
        FormOptionUpdate option1 = new FormOptionUpdate(null, "Option 1");
        FormOptionUpdate option2 = new FormOptionUpdate(null, "Option 2");

        FormQuestionUpdate.SelectOneUpdate question = new FormQuestionUpdate.SelectOneUpdate(
            null,
            "Gender",
            "What is your gender?",
            FormQuestionType.SELECT_ONE,
            false,
            List.of(option1, option2)
        );

        FormUpdate formUpdate = new FormUpdate(
            null,
            "New Form",
            List.of(question)
        );

        // 1 question + 2 options = 3 items de imagen
        List<MultipartFile> imageFiles = Arrays.asList(null, null, null);

        // Act
        boolean result = formService.updateForm(testConfig, FormType.PRE, formUpdate, imageFiles);

        // Assert
        assertTrue(result);
        Optional<FormModel> createdForm = formRepository.findByTypeAndConfigVersion(FormType.PRE, testConfig.getVersion());
        assertTrue(createdForm.isPresent());
        assertEquals("New Form", createdForm.get().getTitle());

        List<FormQuestionModel> questions = formQuestionRepository
            .findAllByFormIdOrderByOrder(createdForm.get().getId());
        assertEquals(1, questions.size());
        assertEquals("What is your gender?", questions.get(0).getText());

        List<FormQuestionOptionModel> options = formQuestionOptionRepository
            .findAllByFormQuestionFormIdOrderByOrder(createdForm.get().getId());
        assertEquals(2, options.size());

        log.info("----------- Test passed: New form created successfully -----------");
    }

    @Test
    @Transactional
    @DisplayName("Should update existing form title")
    void updateForm_WithExistingForm_UpdateTitle_ShouldModify() {
        log.info("----------- Test: updateForm updating title -----------");

        // Arrange - Create initial form
        FormModel existingForm = FormModel.builder()
            .config(testConfig)
            .type(FormType.PRE)
            .title("Old Title")
            .build();
        formRepository.save(existingForm);

        FormQuestionModel question = FormQuestionModel.builder()
            .form(existingForm)
            .order((short) 0)
            .category("Test")
            .text("Question")
            .type(FormQuestionType.SELECT_ONE)
            .required(true)
            .other(false)
            .build();
        formQuestionRepository.save(question);

        FormQuestionOptionModel option = FormQuestionOptionModel.builder()
            .formQuestion(question)
            .order((short) 0)
            .text("Option")
            .image(null)
            .build();
        formQuestionOptionRepository.save(option);

        FormOptionUpdate optionUpdate = new FormOptionUpdate(option.getId(), "Option");
        FormQuestionUpdate.SelectOneUpdate questionUpdate = new FormQuestionUpdate.SelectOneUpdate(
            question.getId(),
            "Test",
            "Question",
            FormQuestionType.SELECT_ONE,
            false,
            List.of(optionUpdate)
        );

        FormUpdate formUpdate = new FormUpdate(
            existingForm.getId(),
            "New Title",
            List.of(questionUpdate)
        );

        List<MultipartFile> imageFiles = Arrays.asList(null, null);

        // Act
        boolean result = formService.updateForm(testConfig, FormType.PRE, formUpdate, imageFiles);

        // Assert
        assertTrue(result);
        Optional<FormModel> updatedForm = formRepository.findByTypeAndConfigVersion(FormType.PRE, testConfig.getVersion());
        assertTrue(updatedForm.isPresent());
        assertEquals("New Title", updatedForm.get().getTitle());

        log.info("----------- Test passed: Form title updated successfully -----------");
    }

    @Test
    @Transactional
    @DisplayName("Should handle no changes to existing form")
    void updateForm_WithNoChanges_ShouldReturnFalse() {
        log.info("----------- Test: updateForm with no changes -----------");

        // Arrange
        FormModel existingForm = FormModel.builder()
            .config(testConfig)
            .type(FormType.PRE)
            .title("Form Title")
            .build();
        formRepository.save(existingForm);

        FormQuestionModel question = FormQuestionModel.builder()
            .form(existingForm)
            .order((short) 0)
            .category("Test")
            .text("Question")
            .type(FormQuestionType.SELECT_ONE)
            .required(true)
            .other(false)
            .build();
        formQuestionRepository.save(question);

        FormQuestionOptionModel option = FormQuestionOptionModel.builder()
            .formQuestion(question)
            .order((short) 0)
            .text("Option")
            .image(null)
            .build();
        formQuestionOptionRepository.save(option);

        FormOptionUpdate optionUpdate = new FormOptionUpdate(option.getId(), "Option");
        FormQuestionUpdate.SelectOneUpdate questionUpdate = new FormQuestionUpdate.SelectOneUpdate(
            question.getId(),
            "Test",
            "Question",
            FormQuestionType.SELECT_ONE,
            false,
            List.of(optionUpdate)
        );

        FormUpdate formUpdate = new FormUpdate(
            existingForm.getId(),
            "Form Title",
            List.of(questionUpdate)
        );

        List<MultipartFile> imageFiles = Arrays.asList(null, null);

        // Act
        boolean result = formService.updateForm(testConfig, FormType.PRE, formUpdate, imageFiles);

        // Assert
        assertFalse(result);


        log.info("----------- Test passed: No modifications returned false correctly -----------");
    }

    @Test
    @Transactional
    @DisplayName("Should create slider with min, max, step")
    void updateForm_WithSliderQuestion_ShouldCreateWithConstraints() {
        log.info("----------- Test: updateForm creating SLIDER question -----------");

        // Arrange
        SliderLabelUpdate label1 = new SliderLabelUpdate(null, 1, "Low");
        SliderLabelUpdate label10 = new SliderLabelUpdate(null, 10, "High");

        FormQuestionUpdate.SliderUpdate sliderQuestion = new FormQuestionUpdate.SliderUpdate(
            null,
            "Satisfaction",
            "How satisfied are you?",
            FormQuestionType.SLIDER,
            1,
            10,
            1,
            List.of(label1, label10)
        );

        FormUpdate formUpdate = new FormUpdate(
            null,
            "Satisfaction Form",
            List.of(sliderQuestion)
        );

        List<MultipartFile> imageFiles = Arrays.asList((MultipartFile) null);

        // Act
        boolean result = formService.updateForm(testConfig, FormType.POST, formUpdate, imageFiles);

        // Assert
        assertTrue(result);
        Optional<FormModel> createdForm = formRepository.findByTypeAndConfigVersion(FormType.POST, testConfig.getVersion());
        assertTrue(createdForm.isPresent());

        List<FormQuestionModel> questions = formQuestionRepository
            .findAllByFormIdOrderByOrder(createdForm.get().getId());
        assertEquals(1, questions.size());

        FormQuestionModel slider = questions.get(0);
        assertEquals(FormQuestionType.SLIDER, slider.getType());
        assertEquals(1, slider.getMin());
        assertEquals(10, slider.getMax());
        assertEquals(1, slider.getStep());
        assertNull(slider.getOther());

        List<FormQuestionSliderLabelModel> labels = formQuestionSliderLabelRepository
            .findAllByFormQuestionFormId(createdForm.get().getId());
        assertEquals(2, labels.size());

        log.info("----------- Test passed: SLIDER question created with constraints -----------");
    }

    @Test
    @Transactional
    @DisplayName("Should create text question with minLength, maxLength, placeholder")
    void updateForm_WithTextQuestion_ShouldCreateWithTextConstraints() {
        log.info("----------- Test: updateForm creating TEXT_LONG question -----------");

        // Arrange
        FormQuestionUpdate.TextLongUpdate textQuestion = new FormQuestionUpdate.TextLongUpdate(
            null,
            "Feedback",
            "Please provide feedback",
            FormQuestionType.TEXT_LONG,
            "Enter your detailed feedback here",
            10,
            500
        );

        FormUpdate formUpdate = new FormUpdate(
            null,
            "Feedback Form",
            List.of(textQuestion)
        );

        List<MultipartFile> imageFiles = Arrays.asList((MultipartFile) null);

        // Act
        boolean result = formService.updateForm(testConfig, FormType.POST, formUpdate, imageFiles);

        // Assert
        assertTrue(result);
        Optional<FormModel> createdForm = formRepository.findByTypeAndConfigVersion(FormType.POST, testConfig.getVersion());
        assertTrue(createdForm.isPresent());

        List<FormQuestionModel> questions = formQuestionRepository
            .findAllByFormIdOrderByOrder(createdForm.get().getId());
        assertEquals(1, questions.size());

        FormQuestionModel textLong = questions.get(0);
        assertEquals(FormQuestionType.TEXT_LONG, textLong.getType());
        assertEquals(10, textLong.getMinLength());
        assertEquals(500, textLong.getMaxLength());
        assertEquals("Enter your detailed feedback here", textLong.getPlaceholder());
        assertNull(textLong.getOther());

        log.info("----------- Test passed: TEXT_LONG question created with constraints -----------");
    }

    @Test
    @Transactional
    @DisplayName("Should create select-multiple with min/max and other flag")
    void updateForm_WithSelectMultipleQuestion_ShouldCreateWithMinMax() {
        log.info("----------- Test: updateForm creating SELECT_MULTIPLE question -----------");

        // Arrange
        FormOptionUpdate opt1 = new FormOptionUpdate(null, "Option 1");
        FormOptionUpdate opt2 = new FormOptionUpdate(null, "Option 2");
        FormOptionUpdate opt3 = new FormOptionUpdate(null, "Option 3");

        FormQuestionUpdate.SelectMultipleUpdate multiQuestion = new FormQuestionUpdate.SelectMultipleUpdate(
            null,
            "Interests",
            "Select your interests",
            FormQuestionType.SELECT_MULTIPLE,
            1,
            3,
            true,
            List.of(opt1, opt2, opt3)
        );

        FormUpdate formUpdate = new FormUpdate(
            null,
            "Multi Form",
            List.of(multiQuestion)
        );

        List<MultipartFile> imageFiles = Arrays.asList(null, null, null, null);

        // Act
        boolean result = formService.updateForm(testConfig, FormType.PRE, formUpdate, imageFiles);

        // Assert
        assertTrue(result);
        Optional<FormModel> createdForm = formRepository.findByTypeAndConfigVersion(FormType.PRE, testConfig.getVersion());
        assertTrue(createdForm.isPresent());

        List<FormQuestionModel> questions = formQuestionRepository
            .findAllByFormIdOrderByOrder(createdForm.get().getId());
        FormQuestionModel multiQuestion_retrieved = questions.get(0);

        assertEquals(FormQuestionType.SELECT_MULTIPLE, multiQuestion_retrieved.getType());
        assertEquals(1, multiQuestion_retrieved.getMin());
        assertEquals(3, multiQuestion_retrieved.getMax());
        assertTrue(multiQuestion_retrieved.getOther());

        List<FormQuestionOptionModel> options = formQuestionOptionRepository
            .findAllByFormQuestionFormIdOrderByOrder(createdForm.get().getId());
        assertEquals(3, options.size());

        log.info("----------- Test passed: SELECT_MULTIPLE question created correctly -----------");
    }

    @Test
    @Transactional
    @DisplayName("Should add image to form question")
    void updateForm_WithImageInQuestion_ShouldSaveImage() {
        log.info("----------- Test: updateForm with image in question -----------");

        // Arrange
        FormOptionUpdate option = new FormOptionUpdate(null, "Option with Image");
        FormQuestionUpdate.SelectOneUpdate question = new FormQuestionUpdate.SelectOneUpdate(
            null,
            "Category",
            "Question with Image",
            FormQuestionType.SELECT_ONE,
            false,
            List.of(option)
        );

        FormUpdate formUpdate = new FormUpdate(
            null,
            "Form with Images",
            List.of(question)
        );

        List<MultipartFile> imageFiles = List.of(mockPngFile, mockPngFile);

        // Act
        boolean result = formService.updateForm(testConfig, FormType.PRE, formUpdate, imageFiles);

        // Assert
        assertTrue(result);
        Optional<FormModel> createdForm = formRepository.findByTypeAndConfigVersion(FormType.PRE, testConfig.getVersion());
        assertTrue(createdForm.isPresent());

        List<FormQuestionModel> questions = formQuestionRepository
            .findAllByFormIdOrderByOrder(createdForm.get().getId());
        assertNotNull(questions.get(0).getImage());

        log.info("----------- Test passed: Image saved correctly -----------");
    }
}