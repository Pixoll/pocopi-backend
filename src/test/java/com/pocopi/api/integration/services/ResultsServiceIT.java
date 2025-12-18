package com.pocopi.api.integration.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pocopi.api.dto.event.OptionSelectionEvent;
import com.pocopi.api.dto.event.QuestionEventLog;
import com.pocopi.api.dto.event.QuestionTimestamp;
import com.pocopi.api.dto.results.TestResult;
import com.pocopi.api.dto.results.TestResultsByConfig;
import com.pocopi.api.dto.results.TestResultsByUser;
import com.pocopi.api.dto.user.User;
import com.pocopi.api.models.config.ConfigModel;
import com.pocopi.api.models.test.UserTestAttemptModel;
import com.pocopi.api.models.test.TestGroupModel;
import com.pocopi.api.models.test.TestPhaseModel;
import com.pocopi.api.models.test.TestQuestionModel;
import com.pocopi.api.models.test.TestOptionModel;
import com.pocopi.api.models.user.Role;
import com.pocopi.api.models.user.UserModel;
import com.pocopi.api.repositories.*;
import com.pocopi.api.services.ResultsService;
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
class ResultsServiceIT {

    @Autowired
    private ResultsService resultsService;

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
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    private final ObjectMapper mapper = new ObjectMapper();

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

    private TestGroupModel createGroup(String label) {
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

        TestOptionModel opt = TestOptionModel.builder().question(q).order((short) 0).text("O").correct(true).build();
        testOptionRepository.save(opt);

        return group;
    }

    private UserModel createUser(String username, boolean anonymous) {
        String longPassword = "a".repeat(60);
        UserModel u = UserModel.builder()
            .username(username)
            .role(Role.USER)
            .anonymous(anonymous)
            .password(longPassword)
            .build();
        return userRepository.save(u);
    }

    private void insertQuestionLog(long attemptId, int questionId, long tsStart, long tsEnd, int duration) {
        Timestamp ts = Timestamp.from(Instant.ofEpochMilli(tsStart));
        jdbcTemplate.update(
            "INSERT INTO user_test_question_log (attempt_id, question_id, timestamp, duration) VALUES (?, ?, ?, ?)",
            attemptId, questionId, ts, duration
        );
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
    void getUserTestResults_WithQuestionAndOptionEvents_ShouldComputeCorrectnessAndSelections() throws Exception {
        // Arrange
        UserModel user = createUser("u_results", true);
        TestGroupModel group = createGroup("G1");

        UserTestAttemptModel attempt = UserTestAttemptModel.builder()
            .user(user)
            .group(group)
            .start(Instant.ofEpochMilli(1_000L))
            .end(Instant.ofEpochMilli(5_000L)) // finished attempt
            .build();
        attempt = userTestAttemptRepository.save(attempt);

        long attemptId = attempt.getId();

        int phaseId = testPhaseRepository.findAllByGroupId(group.getId()).get(0).getId();
        int questionId = testQuestionRepository.findAllByPhaseId(phaseId).get(0).getId();
        int optionId = testOptionRepository.findAllByQuestionId(questionId).get(0).getId();

        insertQuestionLog(attemptId, questionId, 1_000L, 1_500L, 500);
        insertOptionLog(attemptId, optionId, "select", 1_200L);

        // Act
        TestResultsByUser resultsByUser = resultsService.getUserTestResults(user.getId());

        // Assert
        assertNotNull(resultsByUser);
        User uDto = resultsByUser.user();
        assertEquals(user.getUsername(), uDto.username());

        assertFalse(resultsByUser.results().isEmpty());
        TestResultsByConfig trc = resultsByUser.results().get(0);
        assertEquals(attempt.getGroup().getConfig().getVersion(), trc.configVersion());

        assertFalse(trc.attemptsResults().isEmpty());
        TestResult tr = trc.attemptsResults().get(0);

        assertEquals(attemptId, tr.attemptId());
        assertEquals(attempt.getGroup().getLabel(), tr.group());
        assertEquals(1, tr.correctQuestions());
        assertEquals(1, tr.questionsAnswered());
        assertEquals(100.0, tr.accuracy());

        assertFalse(tr.questionEvents().isEmpty());
        QuestionEventLog qLog = tr.questionEvents().get(0);
        assertEquals(questionId, qLog.questionId());
        assertFalse(qLog.skipped());
        assertTrue(qLog.correct());
        assertFalse(qLog.optionSelections().isEmpty());
        OptionSelectionEvent sel = qLog.optionSelections().get(0);
        assertEquals(optionId, sel.optionId());
    }

    @Test
    @Transactional
    void getAttemptResults_NotFoundShouldThrow() {
        // Arrange
        long nonExistingAttempt = 99999L;

        // Act & Assert
        assertThrows(RuntimeException.class, () -> resultsService.getAttemptResults(nonExistingAttempt));
    }

    @Test
    @Transactional
    void getTestResultsFromMultipleAttempts_ShouldGroupByConfigAndReturnAllAttempts() throws Exception {
        // Arrange
        UserModel user = createUser("u_multi", true);
        TestGroupModel g1 = createGroup("GM1");
        TestGroupModel g2 = createGroup("GM2");

        UserTestAttemptModel a1 = UserTestAttemptModel.builder()
            .user(user).group(g1).start(Instant.ofEpochMilli(100)).end(Instant.ofEpochMilli(600)).build();
        a1 = userTestAttemptRepository.save(a1);

        UserTestAttemptModel a2 = UserTestAttemptModel.builder()
            .user(user).group(g2).start(Instant.ofEpochMilli(200)).end(Instant.ofEpochMilli(700)).build();
        a2 = userTestAttemptRepository.save(a2);

        int phase1 = testPhaseRepository.findAllByGroupId(g1.getId()).get(0).getId();
        int qid1 = testQuestionRepository.findAllByPhaseId(phase1).get(0).getId();
        int opt1 = testOptionRepository.findAllByQuestionId(qid1).get(0).getId();
        insertQuestionLog(a1.getId(), qid1, 100L, 150L, 50);
        insertOptionLog(a1.getId(), opt1, "select", 120L);

        int phase2 = testPhaseRepository.findAllByGroupId(g2.getId()).get(0).getId();
        int qid2 = testQuestionRepository.findAllByPhaseId(phase2).get(0).getId();
        int opt2 = testOptionRepository.findAllByQuestionId(qid2).get(0).getId();
        insertQuestionLog(a2.getId(), qid2, 200L, 250L, 50);
        insertOptionLog(a2.getId(), opt2, "select", 220L);

        // Act
        TestResultsByUser results = resultsService.getUserTestResults(user.getId());

        // Assert
        assertNotNull(results);
        assertEquals(user.getUsername(), results.user().username());
        assertFalse(results.results().isEmpty());

        int totalAttempts = results.results().stream().mapToInt(r -> r.attemptsResults().size()).sum();
        assertTrue(totalAttempts >= 2);
    }
}