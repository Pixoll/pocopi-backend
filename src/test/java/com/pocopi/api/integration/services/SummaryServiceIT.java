package com.pocopi.api.integration.services;

import com.pocopi.api.dto.attempt.TestAttemptSummary;
import com.pocopi.api.dto.attempt.TestAttemptsSummary;
import com.pocopi.api.dto.user.User;
import com.pocopi.api.exception.HttpException;
import com.pocopi.api.models.config.ConfigModel;
import com.pocopi.api.models.test.*;
import com.pocopi.api.models.user.Role;
import com.pocopi.api.models.user.UserModel;
import com.pocopi.api.repositories.*;
import com.pocopi.api.services.SummaryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("integration")
class SummaryServiceIT {

    @Autowired
    private SummaryService summaryService;

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

    @Autowired
    private UserTestOptionLogRepository userTestOptionLogRepository;

    @Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

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

    private TestGroupModel createGroupWithOneQuestion(String label, boolean optionCorrect) {
        TestGroupModel group = TestGroupModel.builder()
            .config(cfg)
            .label(label)
            .probability((byte) 100)
            .build();
        group = testGroupRepository.save(group);

        TestPhaseModel phase = TestPhaseModel.builder().group(group).order((short) 0).build();
        phase = testPhaseRepository.save(phase);

        TestQuestionModel q = TestQuestionModel.builder().phase(phase).order((short) 0).text("Q").build();
        q = testQuestionRepository.save(q);

        TestOptionModel opt = TestOptionModel.builder()
            .question(q)
            .order((short) 0)
            .text("O")
            .correct(optionCorrect)
            .build();
        testOptionRepository.save(opt);

        return group;
    }

    private UserModel createUser(String username) {
        String longPassword = "a".repeat(60);
        UserModel u = UserModel.builder()
            .username(username)
            .role(Role.USER)
            .anonymous(true)
            .password(longPassword)
            .build();
        return userRepository.save(u);
    }

    private void insertOptionLog(long attemptId, int optionId, String type, long tsMillis) {
        Timestamp ts = Timestamp.from(Instant.ofEpochMilli(tsMillis));
        jdbcTemplate.update(
            "INSERT INTO user_test_option_log (attempt_id, option_id, `type`, timestamp) VALUES (?, ?, ?, ?)",
            attemptId, optionId, type, ts
        );
    }

    @Test
    @Transactional
    void getUserLatestTestAttemptSummary_WhenNoAttempts_ShouldThrowNotFound() {
        // Arrange
        UserModel user = createUser("user_no_attempts");

        // Act / Assert
        assertThrows(HttpException.class, () -> summaryService.getUserLatestTestAttemptSummary(user.getId()));
    }

    @Test
    @Transactional
    void getUserLatestTestAttemptSummary_WithSingleFinishedAttempt_ShouldReturnCorrectSummary() {
        // Arrange
        UserModel user = createUser("user_single");
        TestGroupModel group = createGroupWithOneQuestion("Gsingle", true);

        UserTestAttemptModel attempt = UserTestAttemptModel.builder()
            .user(user)
            .group(group)
            .start(Instant.ofEpochMilli(1_000L))
            .end(Instant.ofEpochMilli(2_000L))
            .build();
        attempt = userTestAttemptRepository.save(attempt);

        int questionId = testQuestionRepository.findAllByPhaseId(testPhaseRepository.findAllByGroupId(group.getId()).get(0).getId()).get(0).getId();
        int optionId = testOptionRepository.findAllByQuestionId(questionId).get(0).getId();

        insertOptionLog(attempt.getId(), optionId, "select", 1500L);

        // Act
        TestAttemptSummary summary = summaryService.getUserLatestTestAttemptSummary(user.getId());

        // Assert
        assertNotNull(summary);
        assertEquals(attempt.getId(), summary.id());
        User udto = summary.user();
        assertEquals(user.getUsername(), udto.username());
        assertEquals(group.getConfig().getVersion(), summary.configVersion());
        assertEquals(group.getLabel(), summary.group());
        assertEquals(1, summary.questionsAnswered());
        assertEquals(1, summary.correctQuestions());
        assertEquals(100.0, summary.accuracy());
        assertEquals(Math.toIntExact(attempt.getEnd().toEpochMilli() - attempt.getStart().toEpochMilli()), summary.timeTaken());
    }

    @Test
    @Transactional
    void getAllTestAttemptsSummary_ShouldAggregateAcrossAttempts() {
        // Arrange
        UserModel a = createUser("userA");
        TestGroupModel gA = createGroupWithOneQuestion("GA", true);
        UserTestAttemptModel atA = UserTestAttemptModel.builder()
            .user(a).group(gA)
            .start(Instant.ofEpochMilli(1_000)).end(Instant.ofEpochMilli(1_500)).build();
        atA = userTestAttemptRepository.save(atA);
        int qA = testQuestionRepository.findAllByPhaseId(testPhaseRepository.findAllByGroupId(gA.getId()).get(0).getId()).get(0).getId();
        int optA = testOptionRepository.findAllByQuestionId(qA).get(0).getId();
        insertOptionLog(atA.getId(), optA, "select", 1200L);

        UserModel b = createUser("userB");
        TestGroupModel gB = createGroupWithOneQuestion("GB", false);
        UserTestAttemptModel atB = UserTestAttemptModel.builder()
            .user(b).group(gB)
            .start(Instant.ofEpochMilli(2_000)).end(Instant.ofEpochMilli(3_000)).build();
        atB = userTestAttemptRepository.save(atB);
        int qB = testQuestionRepository.findAllByPhaseId(testPhaseRepository.findAllByGroupId(gB.getId()).get(0).getId()).get(0).getId();
        int optB = testOptionRepository.findAllByQuestionId(qB).get(0).getId();
        insertOptionLog(atB.getId(), optB, "select", 2500L);

        // Act
        TestAttemptsSummary summary = summaryService.getAllTestAttemptsSummary();

        // Assert
        assertNotNull(summary);
        assertEquals(2, summary.totalQuestionsAnswered());
        assertEquals(0.5, summary.averageAccuracy(), 1e-6);
        assertEquals(750.0, summary.averageTimeTaken(), 1e-6);

        assertTrue(summary.users().size() >= 2);
    }
}