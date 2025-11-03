package com.pocopi.api.services;

import com.pocopi.api.dto.api.FieldError;
import com.pocopi.api.dto.event.NewOptionEventLog;
import com.pocopi.api.dto.event.NewQuestionEventLog;
import com.pocopi.api.dto.event.OptionEventLog;
import com.pocopi.api.dto.event.QuestionEventLog;
import com.pocopi.api.exception.HttpException;
import com.pocopi.api.exception.MultiFieldException;
import com.pocopi.api.models.test.*;
import com.pocopi.api.repositories.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public List<QuestionEventLog> getAllEventLogs() {
        final int configVersion = configRepository.findLastConfig().getVersion();

        final List<Object[]> allQuestionInfo = userTestQuestionLogRepository.findAllQuestionEvents(configVersion);
        final List<Object[]> allEvents = userTestOptionLogRepository.findAllEventByLastConfig(configVersion);

        return parseEventLogs(allQuestionInfo, allEvents);
    }

    public List<QuestionEventLog> getEventLogsByUserId(int userId) {
        final int configVersion = configRepository.findLastConfig().getVersion();

        final List<Object[]> userQuestionInfo = userTestQuestionLogRepository
            .findAllQuestionEventsInfoByUserId(configVersion, userId);
        final List<Object[]> userEvents = userTestOptionLogRepository
            .findAllEventByUserIdAndConfigVersion(userId, configVersion);

        return parseEventLogs(userQuestionInfo, userEvents);
    }

    @Transactional
    public void saveQuestionEventLog(NewQuestionEventLog questionEventLog, int userId) {
        validateQuestionEventLog(questionEventLog);

        final int configVersion = configRepository.findLastConfig().getVersion();

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
        validateOptionEventLog(optionEventLog);

        final int configVersion = configRepository.findLastConfig().getVersion();

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

    private static List<QuestionEventLog> parseEventLogs(List<Object[]> userQuestionInfo, List<Object[]> userEvents) {
        final Map<String, List<OptionEventLog>> eventsMap = new HashMap<>();

        for (final Object[] event : userEvents) {
            final int questionId = (Integer) event[0];
            final String type = (String) event[1];
            final int optionId = (Integer) event[2];
            final long timestamp = ((Number) event[3]).longValue();
            final int uid = (Integer) event[4];

            final String key = uid + "_" + questionId;
            final OptionEventLog eventResponse = new OptionEventLog(type, optionId, timestamp);

            eventsMap.computeIfAbsent(key, k -> new ArrayList<>()).add(eventResponse);
        }

        final List<QuestionEventLog> response = new ArrayList<>();

        for (final Object[] questionInfo : userQuestionInfo) {
            final int uid = (int) questionInfo[0];
            final int phaseId = (int) questionInfo[1];
            final int questionId = (int) questionInfo[2];
            final long startTimestamp = ((Number) questionInfo[3]).longValue();
            final long endTimestamp = ((Number) questionInfo[4]).longValue();
            final boolean correct = ((Number) questionInfo[5]).intValue() == 1;
            final boolean skipped = ((Number) questionInfo[6]).intValue() == 1;
            final int totalOptionChanges = ((Number) questionInfo[7]).intValue();
            final int totalOptionHovers = ((Number) questionInfo[8]).intValue();

            final String key = uid + "_" + questionId;
            final List<OptionEventLog> events = eventsMap.getOrDefault(key, new ArrayList<>());

            final QuestionEventLog questionEventLogResponse = new QuestionEventLog(
                uid,
                phaseId,
                questionId,
                startTimestamp,
                endTimestamp,
                skipped,
                correct,
                totalOptionChanges,
                totalOptionHovers,
                events
            );

            response.add(questionEventLogResponse);
        }

        return response;
    }

    private static void validateQuestionEventLog(NewQuestionEventLog questionEventLog) {
        final ArrayList<FieldError> errors = new ArrayList<>();

        if (questionEventLog.questionId() == null) {
            errors.add(new FieldError("questionId", "Question id is required"));
        } else if (questionEventLog.questionId() < 1) {
            errors.add(new FieldError("questionId", "Question id must be a positive integer"));
        }

        if (questionEventLog.timestamp() == null) {
            errors.add(new FieldError("timestamp", "Timestamp is required"));
        } else if (questionEventLog.timestamp() <= 0) {
            errors.add(new FieldError("timestamp", "Timestamp must be a positive integer"));
        }

        if (questionEventLog.duration() == null) {
            errors.add(new FieldError("duration", "Duration is required"));
        } else if (questionEventLog.duration() < 0) {
            errors.add(new FieldError("duration", "Duration must be a positive integer"));
        }

        if (!errors.isEmpty()) {
            throw new MultiFieldException("Missing or invalid fields", errors);
        }
    }

    private static void validateOptionEventLog(NewOptionEventLog optionEventLog) {
        final ArrayList<FieldError> errors = new ArrayList<>();

        if (optionEventLog.optionId() == null) {
            errors.add(new FieldError("optionId", "Option id is required"));
        } else if (optionEventLog.optionId() < 1) {
            errors.add(new FieldError("optionId", "Option id must be a positive integer"));
        }

        if (optionEventLog.type() == null) {
            errors.add(new FieldError("type", "Type is required"));
        }

        if (optionEventLog.timestamp() == null) {
            errors.add(new FieldError("timestamp", "Timestamp is required"));
        } else if (optionEventLog.timestamp() <= 0) {
            errors.add(new FieldError("timestamp", "Timestamp must be a positive integer"));
        }

        if (!errors.isEmpty()) {
            throw new MultiFieldException("Missing or invalid fields", errors);
        }
    }
}
