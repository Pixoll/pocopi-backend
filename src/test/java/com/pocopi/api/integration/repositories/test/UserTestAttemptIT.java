package com.pocopi.api.integration.repositories.test;

import com.pocopi.api.models.config.ConfigModel;
import com.pocopi.api.models.test.TestGroupModel;
import com.pocopi.api.models.test.UserTestAttemptModel;
import com.pocopi.api.models.user.Role;
import com.pocopi.api.models.user.UserModel;
import com.pocopi.api.repositories.ConfigRepository;
import com.pocopi.api.repositories.TestGroupRepository;
import com.pocopi.api.repositories.UserRepository;
import com.pocopi.api.repositories.UserTestAttemptRepository;
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
class UserTestAttemptIT {

    private static final Logger log = LoggerFactory.getLogger(UserTestAttemptIT.class);

    @Autowired
    private ConfigRepository configRepository;

    @Autowired
    private TestGroupRepository testGroupRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserTestAttemptRepository userTestAttemptRepository;

    @Test
    @Transactional
    void createAndReadUserTestAttempt() {
        log.info("----------- Iniciando UserTestAttemptIT.createAndReadUserTestAttempt -----------");

        // 1) Config básica
        ConfigModel config = ConfigModel.builder()
            .title("Config integración user_test_attempt")
            .subtitle("Subtítulo attempts")
            .description("Descripción para pruebas de user_test_attempt.")
            .informedConsent("Consentimiento informado para pruebas de attempts.")
            .anonymous(false)
            .build();
        ConfigModel savedConfig = configRepository.save(config);
        assertTrue(savedConfig.getVersion() > 0);

        // 2) TestGroup asociado
        TestGroupModel group = TestGroupModel.builder()
            .config(savedConfig)
            .label("GRUPO-ATTEMPT")
            .probability((byte) 100)
            .greeting("Grupo para pruebas de attempts.")
            .build();
        TestGroupModel savedGroup = testGroupRepository.save(group);
        assertTrue(savedGroup.getId() > 0);

        // 3) User no anónimo
        UserModel user = UserModel.builder()
            .username("uta_user")
            .role(Role.USER)
            .anonymous(false)
            .name("User Test Attempt")
            .email("uta@example.com")
            .age((byte) 30)
            .password("x".repeat(UserModel.ENCRYPTED_PASSWORD_LEN))
            .build();
        UserModel savedUser = userRepository.save(user);
        assertTrue(savedUser.getId() > 0);

        // 4) Attempt para ese user y group
        Instant start = Instant.parse("2025-11-27T23:00:00Z");
        UserTestAttemptModel attempt = UserTestAttemptModel.builder()
            .user(savedUser)
            .group(savedGroup)
            .start(start)
            .end(null)
            .build();

        UserTestAttemptModel savedAttempt = userTestAttemptRepository.save(attempt);
        log.info("Attempt guardado con id={} para user_id={} y group_id={}",
            savedAttempt.getId(), savedUser.getId(), savedGroup.getId());

        assertTrue(savedAttempt.getId() > 0);

        // 5) Lectura por id
        UserTestAttemptModel fetched =
            userTestAttemptRepository.findById(savedAttempt.getId()).orElseThrow();

        assertEquals(savedUser.getId(), fetched.getUser().getId(),
            "El user asociado debe coincidir");
        assertEquals(savedGroup.getId(), fetched.getGroup().getId(),
            "El grupo asociado debe coincidir");
        assertEquals(start, fetched.getStart(),
            "El start debe coincidir");
        assertNull(fetched.getEnd(), "end debe ser null en este caso de prueba");

        // 6) Lectura de todos los attempts del user
        List<UserTestAttemptModel> all = userTestAttemptRepository.findAll();
        long countForUser = all.stream()
            .filter(a -> a.getUser().getId() == savedUser.getId())
            .count();

        assertEquals(1, countForUser,
            "Debe existir exactamente un attempt para este user en el contexto de este test");

        log.info("----------- Finalizó correctamente UserTestAttemptIT.createAndReadUserTestAttempt -----------");
    }
}
