package com.pocopi.api.services;

import com.pocopi.api.dto.event.NewOptionEventLog;
import com.pocopi.api.dto.event.NewQuestionEventLog;
import com.pocopi.api.exception.HttpException;
import com.pocopi.api.models.test.*;
import com.pocopi.api.repositories.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class EventLogService {
    private final ConfigRepository configRepository;
    private final TestQuestionRepository testQuestionRepository;
    private final TestOptionRepository testOptionRepository;
    private final UserTestAttemptRepository userTestAttemptRepository;
    private final UserTestQuestionLogRepository userTestQuestionLogRepository;
    private final UserTestOptionLogRepository userTestOptionLogRepository;

    public EventLogService(
        ConfigRepository configRepository,
        TestQuestionRepository testQuestionRepository,
        TestOptionRepository testOptionRepository,
        UserTestAttemptRepository userTestAttemptRepository,
        UserTestQuestionLogRepository userTestQuestionLogRepository,
        UserTestOptionLogRepository userTestOptionLogRepository
    ) {
        this.configRepository = configRepository;
        this.testQuestionRepository = testQuestionRepository;
        this.testOptionRepository = testOptionRepository;
        this.userTestAttemptRepository = userTestAttemptRepository;
        this.userTestQuestionLogRepository = userTestQuestionLogRepository;
        this.userTestOptionLogRepository = userTestOptionLogRepository;
    }

    @Transactional
    public void saveQuestionEventLog(NewQuestionEventLog questionEventLog, int userId) {
        final int configVersion = configRepository.getLastConfig().getVersion();

        final UserTestAttemptModel testAttempt = userTestAttemptRepository
            .findUnfinishedAttempt(configVersion, userId)
            .orElseThrow(() -> HttpException.notFound("User has not started an attempt yet"));

        final int groupId = testAttempt.getGroup().getId();

        final TestQuestionModel question = testQuestionRepository
            .findByIdAndPhaseGroupId(questionEventLog.questionId(), groupId)
            .orElseThrow(() -> HttpException.notFound(
                "Test question with id " + questionEventLog.questionId() + " not found in group " + groupId
            ));

        final UserTestQuestionLogModel newQuestionLog = UserTestQuestionLogModel.builder()
            .attempt(testAttempt)
            .question(question)
            .timestamp(Instant.ofEpochMilli(questionEventLog.timestamp()))
            .duration(questionEventLog.duration())
            .build();

        userTestQuestionLogRepository.save(newQuestionLog);
    }

    @Transactional
    public void saveOptionEventLog(NewOptionEventLog optionEventLog, int userId) {
        final int configVersion = configRepository.getLastConfig().getVersion();

        final UserTestAttemptModel testAttempt = userTestAttemptRepository
            .findUnfinishedAttempt(configVersion, userId)
            .orElseThrow(() -> HttpException.notFound("User has not started an attempt yet"));

        final int groupId = testAttempt.getGroup().getId();

        final TestOptionModel option = testOptionRepository
            .findByIdAndQuestionPhaseGroupId(optionEventLog.optionId(), groupId)
            .orElseThrow(() -> HttpException.notFound(
                "Test option with id " + optionEventLog.optionId() + " not found in group " + groupId
            ));

        final UserTestOptionLogModel newOptionLog = UserTestOptionLogModel.builder()
            .attempt(testAttempt)
            .option(option)
            .type(optionEventLog.type())
            .timestamp(Instant.ofEpochMilli(optionEventLog.timestamp()))
            .build();

        userTestOptionLogRepository.save(newOptionLog);
    }
}
