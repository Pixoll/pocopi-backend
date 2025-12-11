package com.pocopi.api.integration.repositories.form;

import com.pocopi.api.models.config.ConfigModel;
import com.pocopi.api.models.form.FormModel;
import com.pocopi.api.models.form.FormType;
import com.pocopi.api.models.form.UserFormSubmissionModel;
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
class UserFormSubmissionIT {

    private static final Logger log = LoggerFactory.getLogger(UserFormSubmissionIT.class);

    @Autowired
    private ConfigRepository configRepository;

    @Autowired
    private FormRepository formRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestGroupRepository testGroupRepository;

    @Autowired
    private UserTestAttemptRepository userTestAttemptRepository;

    @Autowired
    private UserFormSubmissionRepository userFormSubmissionRepository;

    @Test
    @Transactional
    void createAndReadUserFormSubmission() {
        log.info("----------- Iniciando UserFormSubmissionIT.createAndReadUserFormSubmission -----------");

        // 1) Config + Form
        ConfigModel config = ConfigModel.builder()
            .title("Config integración user_form_submission")
            .subtitle("Subtítulo integración")
            .description("Descripción para pruebas de user_form_submission.")
            .informedConsent("Consentimiento informado para pruebas de user_form_submission.")
            .anonymous(false)
            .build();
        ConfigModel savedConfig = configRepository.save(config);

        FormModel form = FormModel.builder()
            .config(savedConfig)
            .type(FormType.PRE)
            .title("Formulario PRE integración")
            .build();
        FormModel savedForm = formRepository.save(form);

        // 2) User no anónimo (coherente con CHECK de tabla user)
        UserModel user = UserModel.builder()
            .username("ufs_user")
            .role(Role.USER)
            .anonymous(false)
            .name("User Form Submission")
            .email("ufs@example.com")
            .age((byte) 28)
            .password("x".repeat(UserModel.ENCRYPTED_PASSWORD_LEN))
            .build();
        UserModel savedUser = userRepository.save(user);

        // 3) TestGroup
        TestGroupModel group = TestGroupModel.builder()
            .config(savedConfig)
            .label("GRUPO-UFS")
            .probability((byte) 100)
            .greeting("Grupo para pruebas de envíos de formularios.")
            .build();
        TestGroupModel savedGroup = testGroupRepository.save(group);

        // 4) UserTestAttempt para ese user y grupo
        Instant start = Instant.parse("2025-11-27T21:30:00Z");
        UserTestAttemptModel attempt = UserTestAttemptModel.builder()
            .user(savedUser)
            .group(savedGroup)
            .start(start)
            .end(null)
            .build();
        UserTestAttemptModel savedAttempt = userTestAttemptRepository.save(attempt);

        // 5) UserFormSubmission asociado ese attempt y form
        Instant submissionTime = Instant.parse("2025-11-27T21:35:00Z");

        UserFormSubmissionModel submission = UserFormSubmissionModel.builder()
            .attempt(savedAttempt)
            .form(savedForm)
            .timestamp(submissionTime)
            .build();

        UserFormSubmissionModel savedSubmission = userFormSubmissionRepository.save(submission);
        log.info("UserFormSubmission guardado con id={} para attempt={} y form={}",
            savedSubmission.getId(), savedAttempt.getId(), savedForm.getId());

        assertTrue(savedSubmission.getId() > 0);

        // 6) Lectura por id
        UserFormSubmissionModel fetched =
            userFormSubmissionRepository.findById(savedSubmission.getId()).orElseThrow();

        assertEquals(savedAttempt.getId(), fetched.getAttempt().getId(),
            "El attempt asociado debe coincidir");
        assertEquals(savedForm.getId(), fetched.getForm().getId(),
            "El form asociado debe coincidir");
        assertEquals(submissionTime, fetched.getTimestamp(),
            "El timestamp de la submission debe coincidir");

        // 7) Lectura de todas las submissions y filtrado por attempt
        List<UserFormSubmissionModel> all = userFormSubmissionRepository.findAll();
        long countForAttempt = all.stream()
            .filter(s -> s.getAttempt().getId() == savedAttempt.getId())
            .count();

        assertEquals(1, countForAttempt,
            "Debe existir exactamente una submission para ese attempt en este test");

        log.info("----------- Finalizó correctamente UserFormSubmissionIT.createAndReadUserFormSubmission -----------");
    }
}
