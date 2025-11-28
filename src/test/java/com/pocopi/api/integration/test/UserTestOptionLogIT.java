package com.pocopi.api.integration.testlog;

import com.pocopi.api.models.config.ConfigModel;
import com.pocopi.api.models.test.*;
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
class UserTestOptionLogIT {

    private static final Logger log = LoggerFactory.getLogger(UserTestOptionLogIT.class);

    @Autowired
    private ConfigRepository configRepository;

    @Autowired
    private TestGroupRepository testGroupRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserTestAttemptRepository userTestAttemptRepository;

    @Autowired
    private TestPhaseRepository testPhaseRepository;

    @Autowired
    private TestQuestionRepository testQuestionRepository;

    @Autowired
    private TestOptionRepository testOptionRepository;

    @Autowired
    private UserTestOptionLogRepository userTestOptionLogRepository;

    @Test
    @Transactional
    void createAndReadOptionLogForAttempt() {
        log.info("----------- Iniciando UserTestOptionLogIT.createAndReadOptionLogForAttempt -----------");

        // 1) Config
        ConfigModel config = ConfigModel.builder()
            .title("Config integración option_log")
            .subtitle("Subtítulo option_log")
            .description("Descripción para pruebas de user_test_option_log.")
            .informedConsent("Consentimiento informado para pruebas de option log.")
            .anonymous(false)
            .build();
        ConfigModel savedConfig = configRepository.save(config);

        // 2) Grupo
        TestGroupModel group = TestGroupModel.builder()
            .config(savedConfig)
            .label("GRUPO-OPT-LOG")
            .probability((byte) 100)
            .greeting("Grupo para pruebas de option logs.")
            .build();
        TestGroupModel savedGroup = testGroupRepository.save(group);

        // 3) User y Attempt
        UserModel user = UserModel.builder()
            .username("utol_user")
            .role(Role.USER)
            .anonymous(false)
            .name("User Test Option Log")
            .email("utol@example.com")
            .age((byte) 29)
            .password("x".repeat(UserModel.ENCRYPTED_PASSWORD_LEN))
            .build();
        UserModel savedUser = userRepository.save(user);

        Instant start = Instant.parse("2025-11-27T23:45:00Z");
        UserTestAttemptModel attempt = UserTestAttemptModel.builder()
            .user(savedUser)
            .group(savedGroup)
            .start(start)
            .end(null)
            .build();
        UserTestAttemptModel savedAttempt = userTestAttemptRepository.save(attempt);

        // 4) Fase, Pregunta y Opción asociadas al mismo grupo/config
        TestPhaseModel phase = TestPhaseModel.builder()
            .group(savedGroup)
            .order((short) 1)
            .randomizeQuestions(false)
            .build();
        TestPhaseModel savedPhase = testPhaseRepository.save(phase);

        TestQuestionModel question = TestQuestionModel.builder()
            .phase(savedPhase)
            .order((short) 1)
            .text("Pregunta para logs de opciones")
            .image(null)
            .randomizeOptions(true)
            .build();
        TestQuestionModel savedQuestion = testQuestionRepository.save(question);

        TestOptionModel option = TestOptionModel.builder()
            .question(savedQuestion)
            .order((short) 1)
            .text("Opción log A")
            .image(null)
            .correct(true)
            .build();
        TestOptionModel savedOption = testOptionRepository.save(option);

        // 5) UserTestOptionLog coherente:
        Instant logTime = Instant.parse("2025-11-27T23:45:30Z");

        UserTestOptionLogModel logEntry = UserTestOptionLogModel.builder()
            .attempt(savedAttempt)
            .option(savedOption)
            .type(TestOptionEventType.SELECT)
            .timestamp(logTime)
            .build();

        UserTestOptionLogModel savedLog = userTestOptionLogRepository.save(logEntry);
        log.info("OptionLog guardado con id={} para attempt_id={} y option_id={}",
            savedLog.getId(), savedAttempt.getId(), savedOption.getId());

        assertTrue(savedLog.getId() > 0);

        // 6) Lectura por id
        UserTestOptionLogModel fetched =
            userTestOptionLogRepository.findById((int) savedLog.getId()).orElseThrow();

        assertEquals(savedAttempt.getId(), fetched.getAttempt().getId(),
            "El attempt asociado debe coincidir");
        assertEquals(savedOption.getId(), fetched.getOption().getId(),
            "La opción asociada debe coincidir");
        assertEquals(TestOptionEventType.SELECT, fetched.getType(),
            "El tipo de evento debe ser SELECT");
        assertEquals(logTime, fetched.getTimestamp(),
            "El timestamp del log debe coincidir");

        // 7) Lectura de todos los option logs del attempt
        List<UserTestOptionLogModel> all = userTestOptionLogRepository.findAll();
        long countForAttempt = all.stream()
            .filter(l -> l.getAttempt().getId() == savedAttempt.getId())
            .count();

        assertEquals(1, countForAttempt,
            "Debe existir exactamente un log de opción para este attempt en el test");

        log.info("----------- Finalizó correctamente UserTestOptionLogIT.createAndReadOptionLogForAttempt -----------");
    }
}
