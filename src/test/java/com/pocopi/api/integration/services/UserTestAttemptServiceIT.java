package com.pocopi.api.integration.services;

import com.pocopi.api.dto.attempt.UserTestAttempt;
import com.pocopi.api.exception.HttpException;
import com.pocopi.api.models.config.ConfigModel;
import com.pocopi.api.models.test.*;
import com.pocopi.api.models.user.Role;
import com.pocopi.api.models.user.UserModel;
import com.pocopi.api.repositories.*;
import com.pocopi.api.services.UserTestAttemptService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("integration")
class UserTestAttemptServiceIT {

    @Autowired
    private UserTestAttemptService userTestAttemptService;

    @Autowired
    private ConfigRepository configRepository;

    @Autowired
    private TestGroupRepository testGroupRepository;

    @Autowired
    private TestPhaseRepository testPhaseRepository;

    @Autowired
    private TestQuestionRepository testQuestionRepository;

    @Autowired
    private TestOptionRepository testOptionRepository;

    @Autowired
    private UserTestAttemptRepository userTestAttemptRepository;

    @Autowired
    private UserRepository userRepository;

    @TempDir
    private java.nio.file.Path tempDir;

    private ConfigModel cfg;

    @BeforeEach
    void setUp() {
        cfg = ConfigModel.builder()
            .title("cfg")
            .description("desc")
            .informedConsent("consent")
            .active(true)
            .build();
        cfg = configRepository.save(cfg);
    }

    private TestGroupModel createGroupWithHierarchy(String label) {
        TestGroupModel group = TestGroupModel.builder()
            .config(cfg)
            .label(label)
            .probability((byte)100)
            .build();
        group = testGroupRepository.save(group);

        TestPhaseModel phase = TestPhaseModel.builder()
            .group(group)
            .order((short)0)
            .build();
        phase = testPhaseRepository.save(phase);

        TestQuestionModel q = TestQuestionModel.builder()
            .phase(phase)
            .order((short)0)
            .text("Q")
            .build();
        q = testQuestionRepository.save(q);

        TestOptionModel opt = TestOptionModel.builder()
            .question(q)
            .order((short)0)
            .text("O")
            .correct(true)
            .build();
        testOptionRepository.save(opt);

        return group;
    }

    private TestGroupModel createComplexGroup(String label, int[] questionsPerPhase) {
        TestGroupModel group = TestGroupModel.builder()
            .config(cfg)
            .label(label)
            .probability((byte)100)
            .build();
        group = testGroupRepository.save(group);

        short phaseOrder = 0;
        for (int qCount : questionsPerPhase) {
            TestPhaseModel phase = TestPhaseModel.builder()
                .group(group)
                .order(phaseOrder++)
                .build();
            phase = testPhaseRepository.save(phase);

            for (int qi = 0; qi < qCount; qi++) {
                TestQuestionModel q = TestQuestionModel.builder()
                    .phase(phase)
                    .order((short) qi)
                    .text("Q-" + phase.getOrder() + "-" + qi)
                    .build();
                q = testQuestionRepository.save(q);

                for (short oi = 0; oi < 3; oi++) {
                    TestOptionModel opt = TestOptionModel.builder()
                        .question(q)
                        .order(oi)
                        .text("Opt-" + qi + "-" + oi)
                        .correct(oi == 0)
                        .build();
                    testOptionRepository.save(opt);
                }
            }
        }

        return group;
    }

    private UserModel createUser(String username) {
        String longPassword = "a".repeat(60);
        UserModel user = UserModel.builder()
            .username(username)
            .role(Role.USER)
            .anonymous(true)
            .password(longPassword)
            .build();
        return userRepository.save(user);
    }

    @Test
    @Transactional
    void assertActiveAttempt_WhenNoAttempt_ShouldThrowNotFound() {
        // Arrange
        UserModel user = createUser("u_no_attempt");

        // Act & Assert
        assertThrows(HttpException.class, () -> userTestAttemptService.assertActiveAttempt(user));
    }

    @Test
    @Transactional
    void assertActiveAttempt_WhenAttemptExists_ShouldNotThrow() {
        // Arrange
        UserModel user = createUser("u_has_attempt");
        TestGroupModel group = createGroupWithHierarchy("GA");
        UserTestAttemptModel attempt = UserTestAttemptModel.builder()
            .user(user)
            .group(group)
            .start(Instant.now())
            .build();
        userTestAttemptRepository.save(attempt);

        // Act & Assert
        userTestAttemptService.assertActiveAttempt(user);
    }

    @Test
    @Transactional
    void beginAttempt_WhenNoExistingAttempt_ShouldSaveAttemptAndReturnDto() {
        // Arrange
        UserModel user = createUser("u_begin");
        createGroupWithHierarchy("G1");

        // Act
        UserTestAttempt dto = userTestAttemptService.beginAttempt(user);

        // Assert
        assertNotNull(dto);
        assertFalse(dto.completedTest());
        assertFalse(dto.completedPreTestForm());
        assertFalse(dto.completedPostTestForm());
        assertNotNull(dto.assignedGroup());
        assertEquals(1, userTestAttemptRepository.findAll().size());
        assertTrue(userTestAttemptRepository.findAll().stream().anyMatch(a -> a.getUser().getId() == user.getId()));
    }

    @Test
    @Transactional
    void beginAttempt_WhenAlreadyStarted_ShouldThrowConflict() {
        // Arrange
        UserModel user = createUser("u_already");
        TestGroupModel group = createGroupWithHierarchy("Gx");
        UserTestAttemptModel attempt = UserTestAttemptModel.builder()
            .user(user)
            .group(group)
            .start(Instant.now())
            .build();
        userTestAttemptRepository.save(attempt);

        // Act & Assert
        HttpException ex = assertThrows(HttpException.class, () -> userTestAttemptService.beginAttempt(user));
        assertEquals(409, ex.getStatus().value());
    }

    @Test
    @Transactional
    void continueAttempt_WhenNoAttempt_ShouldThrowNotFound() {
        // Arrange
        UserModel user = createUser("u_no_continue");

        // Act & Assert
        assertThrows(HttpException.class, () -> userTestAttemptService.continueAttempt(user.getId()));
    }

    @Test
    @Transactional
    void continueAttempt_WhenAttemptExists_ShouldReturnDtoWithNoAnswers() {
        // Arrange
        UserModel user = createUser("u_continue");
        TestGroupModel group = createGroupWithHierarchy("GC");
        UserTestAttemptModel attempt = UserTestAttemptModel.builder()
            .user(user)
            .group(group)
            .start(Instant.now())
            .build();
        attempt = userTestAttemptRepository.save(attempt);

        // Act
        UserTestAttempt dto = userTestAttemptService.continueAttempt(user.getId());

        // Assert
        assertNotNull(dto);
        assertFalse(dto.completedTest());
        assertNotNull(dto.assignedGroup());
        assertEquals(0, dto.testAnswers().size());
    }

    @Test
    @Transactional
    void continueAttempt_WithComplexGroup_ShouldComputeTotalQuestionsAndReturnAssigned() {
        // Arrange
        UserModel user = createUser("u_complex");
        // phases: 2, 3, 1 questions => total 6 questions
        TestGroupModel group = createComplexGroup("ComplexG", new int[]{2, 3, 1});
        UserTestAttemptModel attempt = UserTestAttemptModel.builder()
            .user(user)
            .group(group)
            .start(Instant.now())
            .build();
        attempt = userTestAttemptRepository.save(attempt);

        // Act
        UserTestAttempt dto = userTestAttemptService.continueAttempt(user.getId());

        // Assert
        assertNotNull(dto);
        assertNotNull(dto.assignedGroup());
        int totalQuestions = dto.assignedGroup().phases().stream().reduce(0, (subtotal, ph) -> subtotal + ph.questions().size(), Integer::sum);
        assertEquals(6, totalQuestions);
        assertFalse(dto.completedTest());
        assertEquals(0, dto.testAnswers().size());
    }

    @Test
    @Transactional
    void discardAttempt_WhenAttemptExists_ShouldRemoveIt() {
        // Arrange
        UserModel user = createUser("u_discard");
        TestGroupModel group = createGroupWithHierarchy("GD");
        UserTestAttemptModel attempt = UserTestAttemptModel.builder()
            .user(user)
            .group(group)
            .start(Instant.now())
            .build();
        attempt = userTestAttemptRepository.save(attempt);

        // Act
        userTestAttemptService.discardAttempt(user.getId());

        // Assert
        assertFalse(userTestAttemptRepository.findById(attempt.getId()).isPresent());
    }

    @Test
    @Transactional
    void discardAttempt_WhenNoAttempt_ShouldThrowNotFound() {
        // Arrange
        UserModel user = createUser("u_discard_none");

        // Act & Assert
        assertThrows(HttpException.class, () -> userTestAttemptService.discardAttempt(user.getId()));
    }

    @Test
    @Transactional
    void endAttempt_WhenAttemptExists_ShouldSetEndTimestamp() {
        // Arrange
        UserModel user = createUser("u_end");
        TestGroupModel group = createGroupWithHierarchy("GE");
        UserTestAttemptModel attempt = UserTestAttemptModel.builder()
            .user(user)
            .group(group)
            .start(Instant.now())
            .build();
        attempt = userTestAttemptRepository.save(attempt);

        // Act
        userTestAttemptService.endAttempt(user.getId());

        // Assert
        UserTestAttemptModel reloaded = userTestAttemptRepository.findById(attempt.getId()).orElseThrow();
        assertNotNull(reloaded.getEnd());
    }

    @Test
    @Transactional
    void endAttempt_WhenNoAttempt_ShouldThrowNotFound() {
        // Arrange
        UserModel user = createUser("u_end_none");

        // Act & Assert
        assertThrows(HttpException.class, () -> userTestAttemptService.endAttempt(user.getId()));
    }

    @Test
    @Transactional
    void beginAttempt_TwiceSequentially_ShouldThrowConflictOnSecondCall() {
        // Arrange
        UserModel user = createUser("u_twice");
        createGroupWithHierarchy("GT");

        // Act
        UserTestAttempt first = userTestAttemptService.beginAttempt(user);
        assertNotNull(first);

        // Assert
        HttpException ex = assertThrows(HttpException.class, () -> userTestAttemptService.beginAttempt(user));
        assertEquals(409, ex.getStatus().value());
    }
}