package com.pocopi.api.integration.repositories.test;

import com.pocopi.api.models.config.ConfigModel;
import com.pocopi.api.models.test.TestGroupModel;
import com.pocopi.api.models.test.TestPhaseModel;
import com.pocopi.api.models.test.TestQuestionModel;
import com.pocopi.api.models.test.UserTestAttemptModel;
import com.pocopi.api.models.test.UserTestQuestionLogModel;
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
class UserTestQuestionLogIT {

    private static final Logger log = LoggerFactory.getLogger(UserTestQuestionLogIT.class);

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
    private UserTestQuestionLogRepository userTestQuestionLogRepository;

    @Test
    @Transactional
    void createAndReadQuestionLogForAttempt() {
        log.info("----------- Iniciando UserTestQuestionLogIT.createAndReadQuestionLogForAttempt -----------");

        // 1) Config
        ConfigModel config = ConfigModel.builder()
            .title("Config integración question_log")
            .subtitle("Subtítulo question_log")
            .description("Descripción para pruebas de user_test_question_log.")
            .informedConsent("Consentimiento informado para pruebas de question log.")
            .anonymous(false)
            .build();
        ConfigModel savedConfig = configRepository.save(config);

        // 2) Grupo
        TestGroupModel group = TestGroupModel.builder()
            .config(savedConfig)
            .label("GRUPO-LOG")
            .probability((byte) 100)
            .greeting("Grupo para pruebas de question logs.")
            .build();
        TestGroupModel savedGroup = testGroupRepository.save(group);

        // 3) User y Attempt
        UserModel user = UserModel.builder()
            .username("utql_user")
            .role(Role.USER)
            .anonymous(false)
            .name("User Test Question Log")
            .email("utql@example.com")
            .age((byte) 27)
            .password("x".repeat(UserModel.ENCRYPTED_PASSWORD_LEN))
            .build();
        UserModel savedUser = userRepository.save(user);

        Instant start = Instant.parse("2025-11-27T23:30:00Z");
        UserTestAttemptModel attempt = UserTestAttemptModel.builder()
            .user(savedUser)
            .group(savedGroup)
            .start(start)
            .end(null)
            .build();
        UserTestAttemptModel savedAttempt = userTestAttemptRepository.save(attempt);

        // 4) Fase y Pregunta asociadas al mismo grupo/config
        TestPhaseModel phase = TestPhaseModel.builder()
            .group(savedGroup)          // o protocol->group, según tu modelo
            .order((short) 1)
            .randomizeQuestions(false)
            .build();
        TestPhaseModel savedPhase = testPhaseRepository.save(phase);

        TestQuestionModel question = TestQuestionModel.builder()
            .phase(savedPhase)
            .order((short) 1)
            .text("Pregunta log: ¿Ejemplo?")
            .image(null)
            .randomizeOptions(false)
            .build();
        TestQuestionModel savedQuestion = testQuestionRepository.save(question);

        // 5) UserTestQuestionLog coherente:
        Instant logTime = Instant.parse("2025-11-27T23:31:00Z");

        UserTestQuestionLogModel logEntry = UserTestQuestionLogModel.builder()
            .attempt(savedAttempt)
            .question(savedQuestion)
            .timestamp(logTime)
            .duration(1500)   // por ejemplo, 1500 ms
            .build();

        UserTestQuestionLogModel savedLog = userTestQuestionLogRepository.save(logEntry);
        log.info("QuestionLog guardado con id={} para attempt_id={} y question_id={}",
            savedLog.getId(), savedAttempt.getId(), savedQuestion.getId());

        assertTrue(savedLog.getId() > 0);

        // 6) Lectura por id
        UserTestQuestionLogModel fetched =
            userTestQuestionLogRepository.findById(savedLog.getId()).orElseThrow();

        assertEquals(savedAttempt.getId(), fetched.getAttempt().getId(),
            "El attempt asociado debe coincidir");
        assertEquals(savedQuestion.getId(), fetched.getQuestion().getId(),
            "La pregunta asociada debe coincidir");
        assertEquals(logTime, fetched.getTimestamp(),
            "El timestamp del log debe coincidir");
        assertEquals(1500, fetched.getDuration(),
            "La duración debe coincidir");

        // 7) Lectura de todos los logs del attempt
        List<UserTestQuestionLogModel> all = userTestQuestionLogRepository.findAll();
        long countForAttempt = all.stream()
            .filter(l -> l.getAttempt().getId() == savedAttempt.getId())
            .count();

        assertEquals(1, countForAttempt,
            "Debe existir exactamente un log de pregunta para este attempt en el test");

        log.info("----------- Finalizó correctamente UserTestQuestionLogIT.createAndReadQuestionLogForAttempt -----------");
    }
}
