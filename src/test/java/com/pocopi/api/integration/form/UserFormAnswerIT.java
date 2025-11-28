package com.pocopi.api.integration.form;

import com.pocopi.api.models.config.ConfigModel;
import com.pocopi.api.models.form.*;
import com.pocopi.api.models.test.TestGroupModel;
import com.pocopi.api.models.test.UserTestAttemptModel;
import com.pocopi.api.models.user.Role;
import com.pocopi.api.models.user.UserModel;
import com.pocopi.api.repositories.*;
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
class UserFormAnswerIT {

    private static final Logger log = LoggerFactory.getLogger(UserFormAnswerIT.class);

    @Autowired
    private ConfigRepository configRepository;

    @Autowired
    private FormRepository formRepository;

    @Autowired
    private FormQuestionRepository formQuestionRepository;

    @Autowired
    private FormQuestionOptionRepository formQuestionOptionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestGroupRepository testGroupRepository;

    @Autowired
    private UserTestAttemptRepository userTestAttemptRepository;

    @Autowired
    private UserFormSubmissionRepository userFormSubmissionRepository;

    @Autowired
    private UserFormAnswerRepository userFormAnswerRepository;

    @Test
    @Transactional
    void createValidSelectOneAnswer() {
        log.info("----------- Iniciando UserFormAnswerIT.createValidSelectOneAnswer -----------");

        // 1) Config y Form
        ConfigModel config = ConfigModel.builder()
            .title("Config integración user_form_answer")
            .subtitle("Subtítulo respuestas")
            .description("Descripción para pruebas de user_form_answer.")
            .informedConsent("Consentimiento informado para pruebas de respuestas.")
            .anonymous(false)
            .build();
        ConfigModel savedConfig = configRepository.save(config);

        FormModel form = FormModel.builder()
            .config(savedConfig)
            .type(FormType.PRE)
            .title("Formulario PRE respuestas")
            .build();
        FormModel savedForm = formRepository.save(form);

        // 2) Pregunta SELECT_ONE (other = false, resto en null, text != null)
        FormQuestionModel question = FormQuestionModel.builder()
            .form(savedForm)
            .order((short) 1)
            .category("origen")
            .text("¿Cómo supiste de este test?")
            .image(null)
            .required(true)
            .type(FormQuestionType.SELECT_ONE)
            .min(null)
            .max(null)
            .step(null)
            .other(false)
            .minLength(null)
            .maxLength(null)
            .placeholder(null)
            .build();
        FormQuestionModel savedQuestion = formQuestionRepository.save(question);

        // 3) Opción asociada a esa pregunta (para select-one)
        FormQuestionOptionModel option = FormQuestionOptionModel.builder()
            .formQuestion(savedQuestion)
            .order((short) 1)
            .text("Redes sociales")
            .image(null)
            .build();
        FormQuestionOptionModel savedOption = formQuestionOptionRepository.save(option);

        // 4) User, TestGroup, Attempt y Submission (como en el test anterior)
        UserModel user = UserModel.builder()
            .username("ufa_user")
            .role(Role.USER)
            .anonymous(false)
            .name("User Form Answer")
            .email("ufa@example.com")
            .age((byte) 26)
            .password("x".repeat(UserModel.ENCRYPTED_PASSWORD_LEN))
            .build();
        UserModel savedUser = userRepository.save(user);

        TestGroupModel group = TestGroupModel.builder()
            .config(savedConfig)
            .label("GRUPO-UFA")
            .probability((byte) 100)
            .greeting("Grupo para pruebas de respuestas de formularios.")
            .build();
        TestGroupModel savedGroup = testGroupRepository.save(group);

        UserTestAttemptModel attempt = UserTestAttemptModel.builder()
            .user(savedUser)
            .group(savedGroup)
            .start(Instant.parse("2025-11-27T22:00:00Z"))
            .end(null)
            .build();
        UserTestAttemptModel savedAttempt = userTestAttemptRepository.save(attempt);

        UserFormSubmissionModel submission = UserFormSubmissionModel.builder()
            .attempt(savedAttempt)
            .form(savedForm)
            .timestamp(Instant.parse("2025-11-27T22:05:00Z"))
            .build();
        UserFormSubmissionModel savedSubmission = userFormSubmissionRepository.save(submission);

        // 5) Respuesta válida para SELECT_ONE + other = false:
        UserFormAnswerModel answer = UserFormAnswerModel.builder()
            .formSubmission(savedSubmission)
            .question(savedQuestion)
            .option(savedOption)
            .value(null)
            .answer(null)
            .build();

        UserFormAnswerModel savedAnswer = userFormAnswerRepository.save(answer);
        log.info("UserFormAnswer guardado con id={} para form_sub_id={}, question_id={}, option_id={}",
            savedAnswer.getId(), savedSubmission.getId(), savedQuestion.getId(), savedOption.getId());

        assertTrue(savedAnswer.getId() > 0);

        // 6) Lectura por id
        UserFormAnswerModel fetched =
            userFormAnswerRepository.findById(savedAnswer.getId()).orElseThrow();

        assertEquals(savedSubmission.getId(), fetched.getFormSubmission().getId(),
            "La submission asociada debe coincidir");
        assertEquals(savedQuestion.getId(), fetched.getQuestion().getId(),
            "La pregunta asociada debe coincidir");
        assertEquals(savedOption.getId(), fetched.getOption().getId(),
            "La opción asociada debe coincidir");
        assertNull(fetched.getAnswer(), "answer debe ser null para other = false");
        assertNull(fetched.getValue(), "value debe ser null para select-one");

        // 7) Lectura de todas las respuestas para ese form_sub
        List<UserFormAnswerModel> all = userFormAnswerRepository.findAll();
        long countForSubmission = all.stream()
            .filter(a -> a.getFormSubmission().getId() == savedSubmission.getId())
            .count();

        assertEquals(1, countForSubmission,
            "Debe existir exactamente una respuesta para esa submission y pregunta");

        log.info("----------- Finalizó correctamente UserFormAnswerIT.createValidSelectOneAnswer -----------");
    }
}
