package com.pocopi.api.services;

import com.pocopi.api.dto.results.TestQuestionResult;
import com.pocopi.api.dto.results.TestResultByUser;
import com.pocopi.api.dto.results.TestResultsByGroup;
import com.pocopi.api.dto.user.UserBasicInfo;
import com.pocopi.api.models.user.UserModel;
import com.pocopi.api.repositories.ConfigRepository;
import com.pocopi.api.repositories.UserRepository;
import com.pocopi.api.repositories.UserTestQuestionLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TestResultsService {
    private final UserTestQuestionLogRepository userTestQuestionLogRepository;
    private final UserRepository userRepository;
    private final ConfigRepository configRepository;

    @Autowired
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

        final int configVersion = configRepository.findLastConfig().getVersion();
        final List<Object[]> rows = userTestQuestionLogRepository.findAllQuestionEventsInfoByUserId(configVersion, userId);

        final List<TestQuestionResult> questionResults = rows.stream().map(row -> new TestQuestionResult(
            ((Number) row[2]).intValue(),
            ((Number) row[1]).intValue(),
            ((Number) row[3]).longValue(),
            ((Number) row[4]).longValue(),
            ((Number) row[5]).intValue() == 1,
            ((Number) row[6]).intValue() == 1,
            ((Number) row[7]).intValue(),
            ((Number) row[8]).intValue()
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
}
