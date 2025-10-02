package com.pocopi.api.services.implementations;

import com.pocopi.api.dto.TimeLog.SingleEventResponse;
import com.pocopi.api.dto.TimeLog.SingleTimeLogResponse;
import com.pocopi.api.models.ConfigModel;
import com.pocopi.api.repositories.UserTestOptionLogRepository;
import com.pocopi.api.repositories.UserTestQuestionLogRepository;
import com.pocopi.api.services.interfaces.TimeLogsService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TimeLogServiceImp implements TimeLogsService {
    private final ConfigServiceImp configServiceImp;
    private final UserServiceImp userServiceImp;
    private final UserTestQuestionLogRepository userTestQuestionLogRepository;
    private final UserTestOptionLogRepository userTestOptionLogRepository;

    public TimeLogServiceImp(ConfigServiceImp configServiceImp,
                             UserServiceImp userServiceImp,
                             UserTestQuestionLogRepository userTestQuestionLogRepository,
                             UserTestOptionLogRepository userTestOptionLogRepository
    ) {
        this.configServiceImp = configServiceImp;
        this.userServiceImp = userServiceImp;
        this.userTestQuestionLogRepository = userTestQuestionLogRepository;
        this.userTestOptionLogRepository = userTestOptionLogRepository;
    }

    @Override
    public List<SingleTimeLogResponse> getTimeLogs() {
        ConfigModel lastConfig = configServiceImp.findLastConfig();

        List<Object[]> allQuestionInfo = userTestQuestionLogRepository.findAllQuestionEventsInfoByUserId(lastConfig.getVersion());
        List<Object[]> allEvents = userTestOptionLogRepository.findAllEventByLastConfig(lastConfig.getVersion());

        Map<String, List<SingleEventResponse>> eventsMap = new HashMap<>();

        for (Object[] event : allEvents) {
            int questionId = (Integer) event[0];
            String type = (String) event[1];
            int optionId = (Integer) event[2];
            long timestamp = ((Number) event[3]).longValue();
            int userId = (Integer) event[4];

            String key = userId + "_" + questionId;

            SingleEventResponse eventResponse = new SingleEventResponse(type, optionId, timestamp);

            eventsMap.computeIfAbsent(key, k -> new ArrayList<>()).add(eventResponse);
        }

        List<SingleTimeLogResponse> response = new ArrayList<>();

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
            List<SingleEventResponse> events = eventsMap.getOrDefault(key, new ArrayList<>());

            SingleTimeLogResponse timeLogResponse = new SingleTimeLogResponse(
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
}
