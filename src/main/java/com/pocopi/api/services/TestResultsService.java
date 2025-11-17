package com.pocopi.api.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pocopi.api.dto.event.QuestionTimestamp;
import com.pocopi.api.dto.results.TestQuestionResult;
import com.pocopi.api.dto.results.TestResultByUser;
import com.pocopi.api.dto.results.TestResultsByGroup;
import com.pocopi.api.dto.user.UserBasicInfo;
import com.pocopi.api.models.user.UserModel;
import com.pocopi.api.repositories.ConfigRepository;
import com.pocopi.api.repositories.UserRepository;
import com.pocopi.api.repositories.UserTestQuestionLogRepository;
import com.pocopi.api.repositories.projections.QuestionEventProjection;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TestResultsService {
    private final static ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final UserTestQuestionLogRepository userTestQuestionLogRepository;
    private final UserRepository userRepository;
    private final ConfigRepository configRepository;

    public TestResultsService(
        UserTestQuestionLogRepository userTestQuestionLogRepository,
        UserRepository userRepository,
        ConfigRepository configRepository
    ) {
        this.userTestQuestionLogRepository = userTestQuestionLogRepository;
        this.userRepository = userRepository;
        this.configRepository = configRepository;
    }

    public TestResultByUser getUserTestResults(int userId) {
        final UserModel user = userRepository.getUserByUserId(userId);
        if (user == null) {
            throw new IllegalArgumentException("Usuario no encontrado");
        }

        final int configVersion = configRepository.getLastConfig().getVersion();
        final List<QuestionEventProjection> rows = userTestQuestionLogRepository
            .findAllQuestionEventsByUserId(configVersion, userId);

        final List<TestQuestionResult> questionResults = rows.stream().map(event -> new TestQuestionResult(
            event.getQuestionId(),
            parseJsonTimestampArray(event.getTimestampsJson()),
            event.getCorrect() == 1,
            event.getSkipped() == 1,
            event.getTotalOptionChanges().intValue(),
            event.getTotalOptionHovers().intValue()
        )).toList();

        final UserBasicInfo userInfo = new UserBasicInfo(
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getAge() == 0 ? null : (int) user.getAge()
        );

        return new TestResultByUser(userInfo, questionResults);
    }

    public TestResultsByGroup getGroupTestResults(int groupId) {
        final List<UserModel> users = userRepository.getAllUsers(); // TODO changed from getByGroupId
        if (users == null || users.isEmpty()) {
            throw new IllegalArgumentException("No users found for this group");
        }

        final List<TestResultByUser> userResults = users.stream()
            .map(user -> getUserTestResults(user.getId()))
            .toList();

        return new TestResultsByGroup(groupId, userResults);
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
