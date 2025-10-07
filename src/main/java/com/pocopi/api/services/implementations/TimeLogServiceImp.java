package com.pocopi.api.services.implementations;

import com.pocopi.api.dto.TimeLog.Event;
import com.pocopi.api.dto.TimeLog.SendOptionEvent;
import com.pocopi.api.dto.TimeLog.TimeLog;
import com.pocopi.api.models.ConfigModel;
import com.pocopi.api.models.UserModel;
import com.pocopi.api.repositories.UserRepository;
import com.pocopi.api.repositories.UserTestOptionLogRepository;
import com.pocopi.api.repositories.UserTestQuestionLogRepository;
import com.pocopi.api.services.interfaces.TimeLogsService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TimeLogServiceImp implements TimeLogsService {
    private final ConfigServiceImp configServiceImp;
    private final UserRepository userRepository;
    private final UserTestQuestionLogRepository userTestQuestionLogRepository;
    private final UserTestOptionLogRepository userTestOptionLogRepository;

    public TimeLogServiceImp(ConfigServiceImp configServiceImp,
                             UserRepository userRepository,
                             UserTestQuestionLogRepository userTestQuestionLogRepository,
                             UserTestOptionLogRepository userTestOptionLogRepository
    ) {
        this.configServiceImp = configServiceImp;
        this.userRepository = userRepository;
        this.userTestQuestionLogRepository = userTestQuestionLogRepository;
        this.userTestOptionLogRepository = userTestOptionLogRepository;
    }

    @Override
    public List<TimeLog> getTimeLogs() {
        ConfigModel lastConfig = configServiceImp.findLastConfig();

        List<Object[]> allQuestionInfo = userTestQuestionLogRepository.findAllQuestionEvents(lastConfig.getVersion());
        List<Object[]> allEvents = userTestOptionLogRepository.findAllEventByLastConfig(lastConfig.getVersion());

        Map<String, List<Event>> eventsMap = new HashMap<>();

        for (Object[] event : allEvents) {
            int questionId = (Integer) event[0];
            String type = (String) event[1];
            int optionId = (Integer) event[2];
            long timestamp = ((Number) event[3]).longValue();
            int userId = (Integer) event[4];

            String key = userId + "_" + questionId;

            Event eventResponse = new Event(type, optionId, timestamp);

            eventsMap.computeIfAbsent(key, k -> new ArrayList<>()).add(eventResponse);
        }

        List<TimeLog> response = new ArrayList<>();

        for (Object[] questionInfo : allQuestionInfo) {
            int userId = (int) questionInfo[0];
            int phaseId = (int) questionInfo[1];
            int questionId = (int) questionInfo[2];
            long startTimestamp = ((Number) questionInfo[3]).longValue();
            long endTimestamp = ((Number) questionInfo[4]).longValue();
            boolean correct = ((Number) questionInfo[5]).intValue() == 1;
            boolean skipped = ((Number) questionInfo[6]).intValue() == 1;
            int totalOptionChanges = ((Number) questionInfo[7]).intValue();
            int totalOptionHovers = ((Number) questionInfo[8]).intValue();

            String key = userId + "_" + questionId;
            List<Event> events = eventsMap.getOrDefault(key, new ArrayList<>());

            TimeLog timeLogResponse = new TimeLog(
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

    @Override
    public TimeLog getTimeLogByUserId(int userId) {
        ConfigModel lastConfig = configServiceImp.findLastConfig();

        List<Object[]> userQuestionInfo = userTestQuestionLogRepository
            .findAllQuestionEventsInfoByUserId(lastConfig.getVersion(), userId);
        List<Object[]> userEvents = userTestOptionLogRepository
            .findAllEventByUserIdAndConfigVersion(userId, lastConfig.getVersion());

        Map<String, List<Event>> eventsMap = new HashMap<>();

        for (Object[] event : userEvents) {
            int questionId = (Integer) event[0];
            String type = (String) event[1];
            int optionId = (Integer) event[2];
            long timestamp = ((Number) event[3]).longValue();
            int uid = (Integer) event[4];

            String key = uid + "_" + questionId;
            Event eventResponse = new Event(type, optionId, timestamp);

            eventsMap.computeIfAbsent(key, k -> new ArrayList<>()).add(eventResponse);
        }

        List<TimeLog> response = new ArrayList<>();

        for (Object[] questionInfo : userQuestionInfo) {
            int uid = (int) questionInfo[0];
            int phaseId = (int) questionInfo[1];
            int questionId = (int) questionInfo[2];
            long startTimestamp = ((Number) questionInfo[3]).longValue();
            long endTimestamp = ((Number) questionInfo[4]).longValue();
            boolean correct = ((Number) questionInfo[5]).intValue() == 1;
            boolean skipped = ((Number) questionInfo[6]).intValue() == 1;
            int totalOptionChanges = ((Number) questionInfo[7]).intValue();
            int totalOptionHovers = ((Number) questionInfo[8]).intValue();

            String key = uid + "_" + questionId;
            List<Event> events = eventsMap.getOrDefault(key, new ArrayList<>());

            TimeLog timeLogResponse = new TimeLog(
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
    @Override
    public String addTimeLog(SendOptionEvent optionEvent) {
        UserModel savedUser = userRepository.findByUsername(optionEvent.username());
        if(savedUser == null){
            return "Username not found";
        }
        int userId = savedUser.getId();

        userTestOptionLogRepository.insertUserTestOptionLog(
            userId,
            optionEvent.optionId(),
            optionEvent.type()
        );
        return "Event created";
    }

}
