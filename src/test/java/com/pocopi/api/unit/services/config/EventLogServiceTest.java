package com.pocopi.api.unit.services.config;

import com.pocopi.api.dto.event.NewOptionEventLog;
import com.pocopi.api.dto.event.NewQuestionEventLog;
import com.pocopi.api.exception.HttpException;
import com.pocopi.api.models.config.ConfigModel;
import com.pocopi.api.models.test.*;
import com.pocopi.api.repositories.*;
import com.pocopi.api.services.EventLogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventLogServiceTest {

    @Mock
    private ConfigRepository configRepository;

    @Mock
    private TestQuestionRepository testQuestionRepository;

    @Mock
    private TestOptionRepository testOptionRepository;

    @Mock
    private UserTestAttemptRepository userTestAttemptRepository;

    @Mock
    private UserTestQuestionLogRepository userTestQuestionLogRepository;

    @Mock
    private UserTestOptionLogRepository userTestOptionLogRepository;

    @Captor
    private ArgumentCaptor<UserTestQuestionLogModel> questionLogCaptor;

    @Captor
    private ArgumentCaptor<UserTestOptionLogModel> optionLogCaptor;

    private EventLogService eventLogService;

    @BeforeEach
    void setUp() {
        eventLogService = new EventLogService(
            configRepository,
            testQuestionRepository,
            testOptionRepository,
            userTestAttemptRepository,
            userTestQuestionLogRepository,
            userTestOptionLogRepository
        );
    }

    // ==================== saveQuestionEventLog Tests ====================

    @Test
    void saveQuestionEventLog_WithValidAttempt_ShouldSaveLog() {
        // Arrange
        int userId = 1;
        int configVersion = 1;
        int groupId = 10;

        ConfigModel config = ConfigModel.builder().version(configVersion).build();
        TestGroupModel group = TestGroupModel.builder().id(groupId).build();
        UserTestAttemptModel attempt = UserTestAttemptModel.builder().id(1L).group(group).build();
        TestQuestionModel question = TestQuestionModel.builder().id(5).build();

        NewQuestionEventLog eventLog = new NewQuestionEventLog(5, 1000L, 5000);

        when(configRepository.getLastConfig()).thenReturn(config);
        when(userTestAttemptRepository.findUnfinishedAttempt(configVersion, userId))
            .thenReturn(Optional.of(attempt));
        when(testQuestionRepository.findByIdAndPhaseGroupId(5, groupId))
            .thenReturn(Optional.of(question));

        // Act
        eventLogService.saveQuestionEventLog(eventLog, userId);

        // Assert
        verify(userTestQuestionLogRepository, times(1)).save(questionLogCaptor.capture());
        UserTestQuestionLogModel savedLog = questionLogCaptor.getValue();
        assertEquals(attempt, savedLog.getAttempt());
        assertEquals(question, savedLog.getQuestion());
        assertEquals(Instant.ofEpochMilli(1000L), savedLog.getTimestamp());
        assertEquals(5000, savedLog.getDuration());
    }

    @Test
    void saveQuestionEventLog_WithoutUnfinishedAttempt_ShouldThrowNotFound() {
        // Arrange
        int userId = 1;
        int configVersion = 1;
        ConfigModel config = ConfigModel.builder().version(configVersion).build();
        NewQuestionEventLog eventLog = new NewQuestionEventLog(5, 1000L, 5000);

        when(configRepository.getLastConfig()).thenReturn(config);
        when(userTestAttemptRepository.findUnfinishedAttempt(configVersion, userId))
            .thenReturn(Optional.empty());

        // Act & Assert
        HttpException exception = assertThrows(HttpException.class,
            () -> eventLogService.saveQuestionEventLog(eventLog, userId));

        assertTrue(exception.getMessage().contains("User has not started an attempt yet"));
        verify(userTestQuestionLogRepository, never()).save(any());
    }

    @Test
    void saveQuestionEventLog_WithInvalidQuestionForGroup_ShouldThrowNotFound() {
        // Arrange
        int userId = 1;
        int configVersion = 1;
        int groupId = 10;

        ConfigModel config = ConfigModel.builder().version(configVersion).build();
        TestGroupModel group = TestGroupModel.builder().id(groupId).build();
        UserTestAttemptModel attempt = UserTestAttemptModel.builder().id(1L).group(group).build();
        NewQuestionEventLog eventLog = new NewQuestionEventLog(999, 1000L, 5000);

        when(configRepository.getLastConfig()).thenReturn(config);
        when(userTestAttemptRepository.findUnfinishedAttempt(configVersion, userId))
            .thenReturn(Optional.of(attempt));
        when(testQuestionRepository.findByIdAndPhaseGroupId(999, groupId))
            .thenReturn(Optional.empty());

        // Act & Assert
        HttpException exception = assertThrows(HttpException.class,
            () -> eventLogService.saveQuestionEventLog(eventLog, userId));

        assertTrue(exception.getMessage().contains("Test question with id 999 not found in group"));
        verify(userTestQuestionLogRepository, never()).save(any());
    }

    // ==================== saveOptionEventLog Tests ====================

    @Test
    void saveOptionEventLog_WithValidAttempt_ShouldSaveLog() {
        // Arrange
        int userId = 1;
        int configVersion = 1;
        int groupId = 10;

        ConfigModel config = ConfigModel.builder().version(configVersion).build();
        TestGroupModel group = TestGroupModel.builder().id(groupId).build();
        UserTestAttemptModel attempt = UserTestAttemptModel.builder().id(1L).group(group).build();
        TestOptionModel option = TestOptionModel.builder().id(3).build();

        NewOptionEventLog eventLog = new NewOptionEventLog(3, TestOptionEventType.SELECT, 2000L);

        when(configRepository.getLastConfig()).thenReturn(config);
        when(userTestAttemptRepository.findUnfinishedAttempt(configVersion, userId))
            .thenReturn(Optional.of(attempt));
        when(testOptionRepository.findByIdAndQuestionPhaseGroupId(3, groupId))
            .thenReturn(Optional.of(option));

        // Act
        eventLogService.saveOptionEventLog(eventLog, userId);

        // Assert
        verify(userTestOptionLogRepository, times(1)).save(optionLogCaptor.capture());
        UserTestOptionLogModel savedLog = optionLogCaptor.getValue();
        assertEquals(attempt, savedLog.getAttempt());
        assertEquals(option, savedLog.getOption());
        assertEquals(TestOptionEventType.SELECT, savedLog.getType());
        assertEquals(Instant.ofEpochMilli(2000L), savedLog.getTimestamp());
    }

    @Test
    void saveOptionEventLog_WithoutUnfinishedAttempt_ShouldThrowNotFound() {
        // Arrange
        int userId = 1;
        int configVersion = 1;
        ConfigModel config = ConfigModel.builder().version(configVersion).build();
        NewOptionEventLog eventLog = new NewOptionEventLog(3, TestOptionEventType.HOVER, 2000L);

        when(configRepository.getLastConfig()).thenReturn(config);
        when(userTestAttemptRepository.findUnfinishedAttempt(configVersion, userId))
            .thenReturn(Optional.empty());

        // Act & Assert
        HttpException exception = assertThrows(HttpException.class,
            () -> eventLogService.saveOptionEventLog(eventLog, userId));

        assertTrue(exception.getMessage().contains("User has not started an attempt yet"));
        verify(userTestOptionLogRepository, never()).save(any());
    }

    @Test
    void saveOptionEventLog_WithInvalidOptionForGroup_ShouldThrowNotFound() {
        // Arrange
        int userId = 1;
        int configVersion = 1;
        int groupId = 10;

        ConfigModel config = ConfigModel.builder().version(configVersion).build();
        TestGroupModel group = TestGroupModel.builder().id(groupId).build();
        UserTestAttemptModel attempt = UserTestAttemptModel.builder().id(1L).group(group).build();
        NewOptionEventLog eventLog = new NewOptionEventLog(999, TestOptionEventType.DESELECT, 2000L);

        when(configRepository.getLastConfig()).thenReturn(config);
        when(userTestAttemptRepository.findUnfinishedAttempt(configVersion, userId))
            .thenReturn(Optional.of(attempt));
        when(testOptionRepository.findByIdAndQuestionPhaseGroupId(999, groupId))
            .thenReturn(Optional.empty());

        // Act & Assert
        HttpException exception = assertThrows(HttpException.class,
            () -> eventLogService.saveOptionEventLog(eventLog, userId));

        assertTrue(exception.getMessage().contains("Test option with id 999 not found in group"));
        verify(userTestOptionLogRepository, never()).save(any());
    }
}
