package com.pocopi.api.unit.services;

import com.pocopi.api.dto.attempt.UserTestAttempt;
import com.pocopi.api.dto.attempt.UserTestAttemptAnswer;
import com.pocopi.api.dto.test.AssignedTestGroup;
import com.pocopi.api.dto.test.AssignedTestPhase;
import com.pocopi.api.dto.test.AssignedTestQuestion;
import com.pocopi.api.dto.test.AssignedTestOption;
import com.pocopi.api.exception.HttpException;
import com.pocopi.api.models.config.ConfigModel;
import com.pocopi.api.models.test.UserTestAttemptModel;
import com.pocopi.api.models.test.TestGroupModel;
import com.pocopi.api.models.user.UserModel;
import com.pocopi.api.repositories.UserTestAttemptRepository;
import com.pocopi.api.repositories.ConfigRepository;
import com.pocopi.api.repositories.projections.FormsCompletionStatusProjection;
import com.pocopi.api.repositories.projections.TestAnswerProjection;
import com.pocopi.api.services.TestGroupService;
import com.pocopi.api.services.UserTestAttemptService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserTestAttemptServiceTest {

    @Mock
    private ConfigRepository configRepository;

    @Mock
    private TestGroupService testGroupService;

    @Mock
    private UserTestAttemptRepository userTestAttemptRepository;

    private UserTestAttemptService service;

    @BeforeEach
    void setUp() {
        service = new UserTestAttemptService(configRepository, testGroupService, userTestAttemptRepository);
    }

    private ConfigModel activeConfig() {
        return ConfigModel.builder().version(1).build();
    }

    private UserModel sampleUser() {
        return UserModel.builder()
            .id(1)
            .username("u1")
            .password("a".repeat(UserModel.ENCRYPTED_PASSWORD_LEN))
            .build();
    }

    private TestGroupModel sampleGroup() {
        return TestGroupModel.builder().id(11).label("G").probability((byte) 100).build();
    }

    private AssignedTestGroup assignedWithQuestions(int questionsCount) {
        List<AssignedTestQuestion> qlist;
        if (questionsCount == 2) {
            qlist = List.of(
                new AssignedTestQuestion(1, "q1", null, List.of(new AssignedTestOption(10, "o1", null))),
                new AssignedTestQuestion(2, "q2", null, List.of(new AssignedTestOption(11, "o2", null)))
            );
        } else if (questionsCount == 1) {
            qlist = List.of(new AssignedTestQuestion(1, "q1", null, List.of(new AssignedTestOption(10, "o1", null))));
        } else {
            qlist = List.of();
        }
        return new AssignedTestGroup("G", "hi", true, true, true, List.of(new AssignedTestPhase(qlist)));
    }

    @Test
    void assertActiveAttempt_notFound_then_found() {
        when(configRepository.getLastConfig()).thenReturn(activeConfig());
        UserModel user = sampleUser();

        when(userTestAttemptRepository.hasUnfinishedAttempt(activeConfig().getVersion(), user.getId())).thenReturn(false);
        assertThrows(HttpException.class, () -> service.assertActiveAttempt(user));

        when(userTestAttemptRepository.hasUnfinishedAttempt(activeConfig().getVersion(), user.getId())).thenReturn(true);
        assertDoesNotThrow(() -> service.assertActiveAttempt(user));
    }

    @Test
    void beginAttempt_happy_and_conflict_and_sampleException() {
        when(configRepository.getLastConfig()).thenReturn(activeConfig());
        UserModel user = sampleUser();
        TestGroupModel group = sampleGroup();
        AssignedTestGroup assigned = assignedWithQuestions(2);

        when(userTestAttemptRepository.hasUnfinishedAttempt(activeConfig().getVersion(), user.getId())).thenReturn(false);
        when(testGroupService.sampleGroup()).thenReturn(group);
        when(testGroupService.getAssignedGroup(group)).thenReturn(assigned);
        when(userTestAttemptRepository.save(any(UserTestAttemptModel.class))).thenAnswer(inv -> {
            UserTestAttemptModel a = inv.getArgument(0);
            a.setStart(Instant.now());
            return a;
        });

        UserTestAttempt dto = service.beginAttempt(user);
        assertNotNull(dto);
        assertFalse(dto.completedTest());
        assertFalse(dto.completedPreTestForm());
        assertFalse(dto.completedPostTestForm());
        assertTrue(dto.testAnswers().isEmpty());
        assertEquals(assigned, dto.assignedGroup());
        verify(userTestAttemptRepository).save(any());

        when(userTestAttemptRepository.hasUnfinishedAttempt(activeConfig().getVersion(), user.getId())).thenReturn(true);
        assertThrows(HttpException.class, () -> service.beginAttempt(user));

        when(userTestAttemptRepository.hasUnfinishedAttempt(activeConfig().getVersion(), user.getId())).thenReturn(false);
        when(testGroupService.sampleGroup()).thenThrow(new IllegalArgumentException("no groups"));
        assertThrows(IllegalArgumentException.class, () -> service.beginAttempt(user));
    }

    @Test
    void continueAttempt_variousCases() {
        when(configRepository.getLastConfig()).thenReturn(activeConfig());
        UserModel user = sampleUser();

        UserTestAttemptModel unfinished = UserTestAttemptModel.builder()
            .user(user)
            .group(sampleGroup())
            .start(Instant.now())
            .build();

        when(userTestAttemptRepository.findUnfinishedAttempt(activeConfig().getVersion(), user.getId()))
            .thenReturn(Optional.of(unfinished));

        AssignedTestGroup assignedTwo = assignedWithQuestions(2);
        when(testGroupService.getAssignedGroup(unfinished.getGroup())).thenReturn(assignedTwo);

        FormsCompletionStatusProjection formsProj = new FormsCompletionStatusProjection() {
            @Override public int getCompletedPreTestForm() { return 1; }
            @Override public int getCompletedPostTestForm() { return 0; }
        };
        when(userTestAttemptRepository.getFormsCompletionStatus(unfinished.getId())).thenReturn(formsProj);

        TestAnswerProjection a1 = new TestAnswerProjection() {
            @Override public int getQuestionId() { return 1; }
            @Override public int getOptionId() { return 10; }
        };
        TestAnswerProjection a2 = new TestAnswerProjection() {
            @Override public int getQuestionId() { return 2; }
            @Override public int getOptionId() { return 11; }
        };
        when(userTestAttemptRepository.getTestAnswers(unfinished.getId())).thenReturn(List.of(a1, a2));

        UserTestAttempt continued = service.continueAttempt(user.getId());
        assertTrue(continued.completedPreTestForm());
        assertTrue(continued.completedTest());
        assertFalse(continued.completedPostTestForm());
        assertEquals(2, continued.testAnswers().size());
        assertEquals(new UserTestAttemptAnswer(1, 10), continued.testAnswers().get(0));

        when(userTestAttemptRepository.getTestAnswers(unfinished.getId())).thenReturn(List.of(a1));
        UserTestAttempt continuedPartial = service.continueAttempt(user.getId());
        assertFalse(continuedPartial.completedTest());
        assertEquals(1, continuedPartial.testAnswers().size());

        TestAnswerProjection extra = new TestAnswerProjection() {
            @Override public int getQuestionId() { return 3; }
            @Override public int getOptionId() { return 12; }
        };
        when(userTestAttemptRepository.getTestAnswers(unfinished.getId())).thenReturn(List.of(a1, a2, extra));
        UserTestAttempt continuedMore = service.continueAttempt(user.getId());
        assertFalse(continuedMore.completedTest());

        AssignedTestGroup assignedZero = assignedWithQuestions(0);
        when(testGroupService.getAssignedGroup(unfinished.getGroup())).thenReturn(assignedZero);
        when(userTestAttemptRepository.getTestAnswers(unfinished.getId())).thenReturn(List.of());
        UserTestAttempt continuedZero = service.continueAttempt(user.getId());
        assertTrue(continuedZero.completedTest());
    }

    @Test
    void continueAttempt_notFound() {
        when(configRepository.getLastConfig()).thenReturn(activeConfig());
        UserModel user = sampleUser();
        when(userTestAttemptRepository.findUnfinishedAttempt(activeConfig().getVersion(), user.getId())).thenReturn(Optional.empty());
        assertThrows(HttpException.class, () -> service.continueAttempt(user.getId()));
    }

    @Test
    void discardAndEndAttempt_cases() {
        when(configRepository.getLastConfig()).thenReturn(activeConfig());
        UserModel user = sampleUser();
        UserTestAttemptModel attempt = UserTestAttemptModel.builder().user(user).group(sampleGroup()).start(Instant.now()).build();

        when(userTestAttemptRepository.findUnfinishedAttempt(activeConfig().getVersion(), user.getId())).thenReturn(Optional.of(attempt));
        service.discardAttempt(user.getId());
        verify(userTestAttemptRepository).delete(attempt);

        when(userTestAttemptRepository.findUnfinishedAttempt(activeConfig().getVersion(), user.getId())).thenReturn(Optional.empty());
        assertThrows(HttpException.class, () -> service.discardAttempt(user.getId()));

        when(userTestAttemptRepository.findUnfinishedAttempt(activeConfig().getVersion(), user.getId()))
            .thenReturn(Optional.of(attempt));
        service.endAttempt(user.getId());
        verify(userTestAttemptRepository).save(argThat(a -> a.getEnd() != null));

        when(userTestAttemptRepository.findUnfinishedAttempt(activeConfig().getVersion(), user.getId())).thenReturn(Optional.empty());
        assertThrows(HttpException.class, () -> service.endAttempt(user.getId()));
    }
}