package com.pocopi.api.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pocopi.api.dto.event.*;
import com.pocopi.api.exception.HttpException;
import com.pocopi.api.models.test.*;
import com.pocopi.api.repositories.*;
import com.pocopi.api.repositories.projections.OptionEventProjection;
import com.pocopi.api.repositories.projections.OptionEventWithUserIdProjection;
import com.pocopi.api.repositories.projections.QuestionEventProjection;
import com.pocopi.api.repositories.projections.QuestionEventWithUserIdProjection;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
public class EventLogService {
    private final static ObjectMapper OBJECT_MAPPER = new ObjectMapper();

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
    public List<QuestionEventLogWithUserId> getAllEventLogs() {
        final int configVersion = configRepository.findLastConfig().getVersion();

        final List<QuestionEventWithUserIdProjection> questionEvents = userTestQuestionLogRepository
            .findAllQuestionEvents(configVersion);
        final List<OptionEventWithUserIdProjection> optionEvents = userTestOptionLogRepository
            .findAllOptionEvents(configVersion);

        final HashMap<Integer, QuestionEventLogWithUserId> questionEventLogs = new HashMap<>();

        for (final QuestionEventWithUserIdProjection questionEvent : questionEvents) {
            final QuestionEventLogWithUserId questionEventLogResponse = new QuestionEventLogWithUserId(
                questionEvent.getUserId(),
                questionEvent.getQuestionId(),
                parseJsonTimestampArray(questionEvent.getTimestampsJson()),
                questionEvent.getCorrect() == 1,
                questionEvent.getSkipped() == 1,
                questionEvent.getTotalOptionChanges().intValue(),
                questionEvent.getTotalOptionHovers().intValue(),
                new ArrayList<>(),
                new ArrayList<>()
            );

            questionEventLogs.put(questionEvent.getQuestionId(), questionEventLogResponse);
        }

        for (final OptionEventWithUserIdProjection optionEvent : optionEvents) {
            final QuestionEventLogWithUserId questionEvent = questionEventLogs.get(optionEvent.getQuestionId());

            final OptionEventLog eventLog = new OptionEventLog(
                optionEvent.getOptionId(),
                TestOptionEventType.fromValue(optionEvent.getType()),
                optionEvent.getTimestamp()
            );

            questionEvent.events().add(eventLog);

            if (eventLog.type() == TestOptionEventType.SELECT) {
                final OptionSelectionEvent selectionEvent = new OptionSelectionEvent(
                    eventLog.optionId(),
                    eventLog.timestamp()
                );

                questionEvent.optionSelections().add(selectionEvent);
            }
        }

        return questionEventLogs.values().stream().toList();
    }

    @Transactional
    public List<QuestionEventLog> getEventLogsByUserId(int userId) {
        final int configVersion = configRepository.findLastConfig().getVersion();

        final List<QuestionEventProjection> questionEvents = userTestQuestionLogRepository
            .findAllQuestionEventsByUserId(configVersion, userId);
        final List<OptionEventProjection> optionEvents = userTestOptionLogRepository
            .findAllOptionEventsByUserId(configVersion, userId);

        final HashMap<Integer, QuestionEventLog> questionEventLogs = new HashMap<>();

        for (final QuestionEventProjection questionEvent : questionEvents) {
            final QuestionEventLog questionEventLogResponse = new QuestionEventLog(
                questionEvent.getQuestionId(),
                parseJsonTimestampArray(questionEvent.getTimestampsJson()),
                questionEvent.getCorrect() == 1,
                questionEvent.getSkipped() == 1,
                questionEvent.getTotalOptionChanges().intValue(),
                questionEvent.getTotalOptionHovers().intValue(),
                new ArrayList<>(),
                new ArrayList<>()
            );

            questionEventLogs.put(questionEvent.getQuestionId(), questionEventLogResponse);
        }

        for (final OptionEventProjection optionEvent : optionEvents) {
            final QuestionEventLog questionEvent = questionEventLogs.get(optionEvent.getQuestionId());

            final OptionEventLog eventLog = new OptionEventLog(
                optionEvent.getOptionId(),
                TestOptionEventType.fromValue(optionEvent.getType()),
                optionEvent.getTimestamp()
            );

            questionEvent.events().add(eventLog);

            if (eventLog.type() == TestOptionEventType.SELECT) {
                final OptionSelectionEvent selectionEvent = new OptionSelectionEvent(
                    eventLog.optionId(),
                    eventLog.timestamp()
                );

                questionEvent.optionSelections().add(selectionEvent);
            }
        }

        return questionEventLogs.values().stream().toList();
    }

    @Transactional
    public void saveQuestionEventLog(NewQuestionEventLog questionEventLog, int userId) {
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

    private static List<QuestionTimestamp> parseJsonTimestampArray(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }

        try {
            return OBJECT_MAPPER.readValue(
                json, new TypeReference<>() {
                }
            );
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse timestamps JSON", e);
        }
    }
}
