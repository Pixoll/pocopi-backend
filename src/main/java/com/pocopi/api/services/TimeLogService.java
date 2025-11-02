package com.pocopi.api.services;

import com.pocopi.api.dto.time_log.NewTimeLogEvent;
import com.pocopi.api.dto.time_log.TimeLog;
import com.pocopi.api.dto.time_log.TimeLogEvent;
import com.pocopi.api.models.config.ConfigModel;
import com.pocopi.api.models.user.UserModel;
import com.pocopi.api.repositories.UserRepository;
import com.pocopi.api.repositories.UserTestOptionLogRepository;
import com.pocopi.api.repositories.UserTestQuestionLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TimeLogService {
    private final ConfigService configService;
    private final UserRepository userRepository;
    private final UserTestQuestionLogRepository userTestQuestionLogRepository;
    private final UserTestOptionLogRepository userTestOptionLogRepository;

    public TimeLogService(
        ConfigService configService,
        UserRepository userRepository,
        UserTestQuestionLogRepository userTestQuestionLogRepository,
        UserTestOptionLogRepository userTestOptionLogRepository
    ) {
        this.configService = configService;
        this.userRepository = userRepository;
        this.userTestQuestionLogRepository = userTestQuestionLogRepository;
        this.userTestOptionLogRepository = userTestOptionLogRepository;
    }

    public List<TimeLog> getTimeLogs() {
        final ConfigModel lastConfig = configService.findLastConfig();

        final List<Object[]> allQuestionInfo = userTestQuestionLogRepository.findAllQuestionEvents(lastConfig.getVersion());
        final List<Object[]> allEvents = userTestOptionLogRepository.findAllEventByLastConfig(lastConfig.getVersion());

        final Map<String, List<TimeLogEvent>> eventsMap = new HashMap<>();

        for (final Object[] event : allEvents) {
            final int questionId = (Integer) event[0];
            final String type = (String) event[1];
            final int optionId = (Integer) event[2];
            final long timestamp = ((Number) event[3]).longValue();
            final int userId = (Integer) event[4];

            final String key = userId + "_" + questionId;

            final TimeLogEvent eventResponse = new TimeLogEvent(type, optionId, timestamp);

            eventsMap.computeIfAbsent(key, k -> new ArrayList<>()).add(eventResponse);
        }

        final List<TimeLog> response = new ArrayList<>();

        for (final Object[] questionInfo : allQuestionInfo) {
            final int userId = (int) questionInfo[0];
            final int phaseId = (int) questionInfo[1];
            final int questionId = (int) questionInfo[2];
            final long startTimestamp = ((Number) questionInfo[3]).longValue();
            final long endTimestamp = ((Number) questionInfo[4]).longValue();
            final boolean correct = ((Number) questionInfo[5]).intValue() == 1;
            final boolean skipped = ((Number) questionInfo[6]).intValue() == 1;
            final int totalOptionChanges = ((Number) questionInfo[7]).intValue();
            final int totalOptionHovers = ((Number) questionInfo[8]).intValue();

            final String key = userId + "_" + questionId;
            final List<TimeLogEvent> events = eventsMap.getOrDefault(key, new ArrayList<>());

            final TimeLog timeLogResponse = new TimeLog(
                userId,
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

            response.add(timeLogResponse);
        }

        return response;
    }

    public TimeLog getTimeLogByUserId(int userId) {
        final ConfigModel lastConfig = configService.findLastConfig();

        final List<Object[]> userQuestionInfo = userTestQuestionLogRepository
            .findAllQuestionEventsInfoByUserId(lastConfig.getVersion(), userId);
        final List<Object[]> userEvents = userTestOptionLogRepository
            .findAllEventByUserIdAndConfigVersion(userId, lastConfig.getVersion());

        final Map<String, List<TimeLogEvent>> eventsMap = new HashMap<>();

        for (final Object[] event : userEvents) {
            final int questionId = (Integer) event[0];
            final String type = (String) event[1];
            final int optionId = (Integer) event[2];
            final long timestamp = ((Number) event[3]).longValue();
            final int uid = (Integer) event[4];

            final String key = uid + "_" + questionId;
            final TimeLogEvent eventResponse = new TimeLogEvent(type, optionId, timestamp);

            eventsMap.computeIfAbsent(key, k -> new ArrayList<>()).add(eventResponse);
        }

        final List<TimeLog> response = new ArrayList<>();

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
            final List<TimeLogEvent> events = eventsMap.getOrDefault(key, new ArrayList<>());

            final TimeLog timeLogResponse = new TimeLog(
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

            response.add(timeLogResponse);
        }

        if (response.isEmpty()) {
            return null;
        }

        return response.getFirst();
    }

    @Transactional
    public String addTimeLog(NewTimeLogEvent optionEvent) {
        final UserModel savedUser = userRepository.findByUsername(optionEvent.username());
        if (savedUser == null) {
            return "Username not found";
        }
        final int userId = savedUser.getId();

        userTestOptionLogRepository.insertUserTestOptionLog(
            userId,
            optionEvent.optionId(),
            optionEvent.type()
        );
        return "Event created";
    }
}
