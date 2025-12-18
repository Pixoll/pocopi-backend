package com.pocopi.api.integration.services.form;

import com.pocopi.api.dto.form.NewFormAnswer;
import com.pocopi.api.dto.form.NewFormAnswers;
import com.pocopi.api.dto.results.FormSubmissionsByConfig;
import com.pocopi.api.exception.HttpException;
import com.pocopi.api.models.config.ConfigModel;
import com.pocopi.api.models.form.*;
import com.pocopi.api.models.test.TestGroupModel;
import com.pocopi.api.models.test.UserTestAttemptModel;
import com.pocopi.api.models.user.UserModel;
import com.pocopi.api.repositories.*;
import com.pocopi.api.services.FormAnswerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
@ActiveProfiles("integration")
class FormAnswerServiceIT {

    private static final Logger log = LoggerFactory.getLogger(FormAnswerServiceIT.class);

    @Autowired
    private FormAnswerService formAnswerService;

    @Autowired
    private ConfigRepository configRepository;

    @Autowired
    private FormRepository formRepository;

    @Autowired
    private FormQuestionRepository formQuestionRepository;

    @Autowired
    private FormQuestionOptionRepository formQuestionOptionRepository;

    @Autowired
    private UserFormSubmissionRepository userFormSubmissionRepository;

    @Autowired
    private UserFormAnswerRepository userFormAnswerRepository;

    @Autowired
    private UserTestAttemptRepository userTestAttemptRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestGroupRepository testGroupRepository;

    private ConfigModel testConfig;
    private FormModel preForm;
    private FormModel postForm;
    private UserTestAttemptModel testAttempt;
    private UserModel testUser;
    private TestGroupModel testGroup;

    @BeforeEach
    void setUp() {
        // Hash BCrypt válido de exactamente 60 caracteres
        String validBCryptHash = "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";

        testUser = UserModel.builder()
            .username("testuser")
            .email("test@example.com")
            .name("Test User")        // REQUERIDO si anonymous = false
            .age((byte) 25)           // REQUERIDO si anonymous = false
            .password(validBCryptHash)
            .anonymous(false)
            .build();
        testUser = userRepository.save(testUser);

        testConfig = ConfigModel.builder()
            .title("Test Config")
            .subtitle("Test Subtitle")
            .description("Test Description")
            .informedConsent("Test Informed Consent")
            .anonymous(true)
            .build();
        testConfig = configRepository.save(testConfig);

        testGroup = TestGroupModel.builder()
            .config(testConfig)
            .label("Test Group")
            .probability((byte) 50)
            .greeting("Welcome to the test")
            .allowPreviousPhase(true)
            .allowPreviousQuestion(true)
            .allowSkipQuestion(true)
            .randomizePhases(false)
            .build();
        testGroup = testGroupRepository.save(testGroup);

        preForm = FormModel.builder()
            .config(testConfig)
            .title("Pre Form")
            .type(FormType.PRE)
            .build();
        preForm = formRepository.save(preForm);

        postForm = FormModel.builder()
            .config(testConfig)
            .title("Post Form")
            .type(FormType.POST)
            .build();
        postForm = formRepository.save(postForm);

        testAttempt = UserTestAttemptModel.builder()
            .user(testUser)
            .group(testGroup)
            .start(Instant.now())
            .end(null)
            .build();
        testAttempt = userTestAttemptRepository.save(testAttempt);
    }

    // ==================== getUserFormAnswers Tests ====================

    @Test
    @Transactional
    void getUserFormAnswers_WithNoAnswers_ShouldReturnEmptyList() {
        log.info("----------- Iniciando getUserFormAnswers_WithNoAnswers_ShouldReturnEmptyList -----------");

        // Act
        List<FormSubmissionsByConfig> result = formAnswerService.getUserFormAnswers(testUser.getId());

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());

        log.info("----------- Finalizó correctamente getUserFormAnswers_WithNoAnswers_ShouldReturnEmptyList -----------");
    }

    @Test
    @Transactional
    void getUserFormAnswers_WithAnswers_ShouldReturnGroupedByConfig() {
        log.info("----------- Iniciando getUserFormAnswers_WithAnswers_ShouldReturnGroupedByConfig -----------");

        // Arrange
        FormQuestionModel question = FormQuestionModel.builder()
            .form(preForm)
            .order((short) 0)
            .category("test")
            .text("Test Q")
            .type(FormQuestionType.TEXT_SHORT)
            .minLength(1)
            .maxLength(50)
            .placeholder("ph")
            .required(true)
            .build();
        question = formQuestionRepository.save(question);

        testAttempt.setUser(testUser);
        testAttempt.setEnd(Instant.now());
        testAttempt = userTestAttemptRepository.save(testAttempt);

        UserFormSubmissionModel submission = UserFormSubmissionModel.builder()
            .attempt(testAttempt)
            .form(preForm)
            .timestamp(Instant.now())
            .build();
        submission = userFormSubmissionRepository.save(submission);

        UserFormAnswerModel answer = UserFormAnswerModel.builder()
            .formSubmission(submission)
            .question(question)
            .option(null)
            .value(null)
            .answer("Test Answer")
            .build();
        userFormAnswerRepository.save(answer);

        // Act
        List<FormSubmissionsByConfig> result = formAnswerService.getUserFormAnswers(testUser.getId());

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).preTestForm().size() > 0);

        log.info("----------- Finalizó correctamente getUserFormAnswers_WithAnswers_ShouldReturnGroupedByConfig -----------");
    }

    // ==================== saveUserFormAnswers Tests ====================

    @Test
    @Transactional
    void saveUserFormAnswers_TextShortQuestion_ShouldSucceed() {
        log.info("----------- Iniciando saveUserFormAnswers_TextShortQuestion_ShouldSucceed -----------");

        // Arrange
        FormQuestionModel question = FormQuestionModel.builder()
            .form(preForm)
            .order((short) 0)
            .category("contact")
            .text("What is your name?")
            .type(FormQuestionType.TEXT_SHORT)
            .minLength(1)
            .maxLength(100)
            .placeholder("Name")
            .required(true)
            .build();
        question = formQuestionRepository.save(question);

        final int questionId = question.getId();
        NewFormAnswer answer = new NewFormAnswer(questionId, null, null, "John Doe");
        NewFormAnswers formAnswers = new NewFormAnswers(List.of(answer));

        // Act
        formAnswerService.saveUserFormAnswers(testUser.getId(), FormType.PRE, formAnswers);

        // Assert
        List<UserFormAnswerModel> saved = userFormAnswerRepository.findAll().stream()
            .filter(a -> a.getQuestion().getId() == questionId)
            .toList();
        assertEquals(1, saved.size());
        assertEquals("John Doe", saved.get(0).getAnswer());

        log.info("----------- Finalizó correctamente saveUserFormAnswers_TextShortQuestion_ShouldSucceed -----------");
    }

    @Test
    @Transactional
    void saveUserFormAnswers_SliderQuestion_ShouldSucceed() {
        log.info("----------- Iniciando saveUserFormAnswers_SliderQuestion_ShouldSucceed -----------");

        // Arrange
        FormQuestionModel question = FormQuestionModel.builder()
            .form(preForm)
            .order((short) 0)
            .category("satisfaction")
            .text("Rate your satisfaction")
            .type(FormQuestionType.SLIDER)
            .min(0)
            .max(100)
            .step(10)
            .required(true)
            .build();
        question = formQuestionRepository.save(question);

        final int questionId = question.getId();
        NewFormAnswer answer = new NewFormAnswer(questionId, null, 75, null);
        NewFormAnswers formAnswers = new NewFormAnswers(List.of(answer));

        // Act
        formAnswerService.saveUserFormAnswers(testUser.getId(), FormType.PRE, formAnswers);

        // Assert
        List<UserFormAnswerModel> saved = userFormAnswerRepository.findAll().stream()
            .filter(a -> a.getQuestion().getId() == questionId)
            .toList();
        assertEquals(1, saved.size());
        assertEquals(75, saved.get(0).getValue());

        log.info("----------- Finalizó correctamente saveUserFormAnswers_SliderQuestion_ShouldSucceed -----------");
    }

    @Test
    @Transactional
    void saveUserFormAnswers_SelectOneQuestion_ShouldSucceed() {
        log.info("----------- Iniciando saveUserFormAnswers_SelectOneQuestion_ShouldSucceed -----------");

        // Arrange
        FormQuestionModel question = FormQuestionModel.builder()
            .form(preForm)
            .order((short) 0)
            .category("choice")
            .text("Pick one")
            .type(FormQuestionType.SELECT_ONE)
            .other(false)
            .required(true)
            .build();
        question = formQuestionRepository.save(question);

        FormQuestionOptionModel option = FormQuestionOptionModel.builder()
            .formQuestion(question)
            .order((short) 0)
            .text("Option 1")
            .build();
        option = formQuestionOptionRepository.save(option);

        final int questionId = question.getId();
        NewFormAnswer answer = new NewFormAnswer(questionId, option.getId(), null, null);
        NewFormAnswers formAnswers = new NewFormAnswers(List.of(answer));

        // Act
        formAnswerService.saveUserFormAnswers(testUser.getId(), FormType.PRE, formAnswers);

        // Assert
        List<UserFormAnswerModel> saved = userFormAnswerRepository.findAll().stream()
            .filter(a -> a.getQuestion().getId() == questionId)
            .toList();
        assertEquals(1, saved.size());
        assertEquals(option.getId(), saved.get(0).getOption().getId());

        log.info("----------- Finalizó correctamente saveUserFormAnswers_SelectOneQuestion_ShouldSucceed -----------");
    }

    @Test
    @Transactional
    void saveUserFormAnswers_SelectMultipleQuestions_ShouldSucceed() {
        log.info("----------- Iniciando saveUserFormAnswers_SelectMultipleQuestions_ShouldSucceed -----------");

        // Arrange
        FormQuestionModel question = FormQuestionModel.builder()
            .form(preForm)
            .order((short) 0)
            .category("demographics")
            .text("Select multiple")
            .type(FormQuestionType.SELECT_MULTIPLE)
            .min(1)
            .max(3)
            .other(false)
            .required(true)
            .build();
        question = formQuestionRepository.save(question);

        FormQuestionOptionModel opt1 = FormQuestionOptionModel.builder()
            .formQuestion(question)
            .order((short) 0)
            .text("Option 1")
            .build();
        FormQuestionOptionModel opt2 = FormQuestionOptionModel.builder()
            .formQuestion(question)
            .order((short) 1)
            .text("Option 2")
            .build();
        opt1 = formQuestionOptionRepository.save(opt1);
        opt2 = formQuestionOptionRepository.save(opt2);

        final int questionId = question.getId();
        NewFormAnswer ans1 = new NewFormAnswer(questionId, opt1.getId(), null, null);
        NewFormAnswer ans2 = new NewFormAnswer(questionId, opt2.getId(), null, null);
        NewFormAnswers formAnswers = new NewFormAnswers(List.of(ans1, ans2));

        // Act
        formAnswerService.saveUserFormAnswers(testUser.getId(), FormType.PRE, formAnswers);

        // Assert
        List<UserFormAnswerModel> saved = userFormAnswerRepository.findAll().stream()
            .filter(a -> a.getQuestion().getId() == questionId)
            .toList();
        assertEquals(2, saved.size());

        log.info("----------- Finalizó correctamente saveUserFormAnswers_SelectMultipleQuestions_ShouldSucceed -----------");
    }

    @Test
    @Transactional
    void saveUserFormAnswers_TextLongQuestion_ShouldSucceed() {
        log.info("----------- Iniciando saveUserFormAnswers_TextLongQuestion_ShouldSucceed -----------");

        // Arrange
        FormQuestionModel question = FormQuestionModel.builder()
            .form(postForm)
            .order((short) 0)
            .category("feedback")
            .text("Your feedback")
            .type(FormQuestionType.TEXT_LONG)
            .minLength(10)
            .maxLength(500)
            .placeholder("Feedback")
            .required(true)
            .build();
        question = formQuestionRepository.save(question);

        String longAnswer = "This is a longer feedback response that exceeds the short text limit.";
        final int questionId = question.getId();
        NewFormAnswer answer = new NewFormAnswer(questionId, null, null, longAnswer);
        NewFormAnswers formAnswers = new NewFormAnswers(List.of(answer));

        // Act
        formAnswerService.saveUserFormAnswers(testUser.getId(), FormType.POST, formAnswers);

        // Assert
        List<UserFormAnswerModel> saved = userFormAnswerRepository.findAll().stream()
            .filter(a -> a.getQuestion().getId() == questionId)
            .toList();
        assertEquals(1, saved.size());
        assertEquals(longAnswer, saved.get(0).getAnswer());

        log.info("----------- Finalizó correctamente saveUserFormAnswers_TextLongQuestion_ShouldSucceed -----------");
    }

    @Test
    @Transactional
    void saveUserFormAnswers_DuplicateFormSubmission_ShouldThrow() {
        log.info("----------- Iniciando saveUserFormAnswers_DuplicateFormSubmission_ShouldThrow -----------");

        // Arrange
        FormQuestionModel question = FormQuestionModel.builder()
            .form(preForm)
            .order((short) 0)
            .category("test")
            .text("Test")
            .type(FormQuestionType.TEXT_SHORT)
            .minLength(1)
            .maxLength(50)
            .placeholder("ph")
            .required(true)
            .build();
        question = formQuestionRepository.save(question);

        final int questionId = question.getId();
        NewFormAnswer answer = new NewFormAnswer(questionId, null, null, "Test");
        NewFormAnswers formAnswers = new NewFormAnswers(List.of(answer));

        formAnswerService.saveUserFormAnswers(testUser.getId(), FormType.PRE, formAnswers);

        // Act & Assert
        HttpException exception = assertThrows(HttpException.class, () ->
            formAnswerService.saveUserFormAnswers(testUser.getId(), FormType.PRE, formAnswers)
        );
        assertTrue(exception.getMessage().contains("already answered"));

        log.info("----------- Finalizó correctamente saveUserFormAnswers_DuplicateFormSubmission_ShouldThrow -----------");
    }


}
