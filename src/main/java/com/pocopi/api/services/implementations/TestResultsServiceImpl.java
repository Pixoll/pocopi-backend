package com.pocopi.api.services.implementations;

import com.pocopi.api.dto.Results.UserBasicInfoResponse;
import com.pocopi.api.dto.TestResult.UserTestResultsWithInfoResponse;
import com.pocopi.api.dto.TestResult.GroupTestResultsResponse;
import com.pocopi.api.dto.TestResult.TestQuestionResult;
import com.pocopi.api.models.user.UserModel;
import com.pocopi.api.repositories.ConfigRepository;
import com.pocopi.api.repositories.UserRepository;
import com.pocopi.api.repositories.UserTestQuestionLogRepository;
import com.pocopi.api.services.interfaces.TestResultsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TestResultsServiceImpl implements TestResultsService {
    private final UserTestQuestionLogRepository userTestQuestionLogRepository;
    private final UserRepository userRepository;
    private final ConfigRepository configRepository;

    @Autowired
    public TestResultsServiceImpl(
            UserTestQuestionLogRepository userTestQuestionLogRepository,
            UserRepository userRepository,
            ConfigRepository configRepository
    ) {
        this.userTestQuestionLogRepository = userTestQuestionLogRepository;
        this.userRepository = userRepository;
        this.configRepository = configRepository;
    }

    @Override
    public UserTestResultsWithInfoResponse getUserTestResults(int userId) {
        UserModel user = userRepository.getUserByUserId(userId);
        if (user == null) throw new IllegalArgumentException("Usuario no encontrado");

        int configVersion = configRepository.findLastConfig().getVersion();
        List<Object[]> rows = userTestQuestionLogRepository.findAllQuestionEventsInfoByUserId(configVersion, userId);

        List<TestQuestionResult> questionResults = rows.stream().map(row -> new TestQuestionResult(
                ((Number)row[2]).intValue(),
                ((Number)row[1]).intValue(),
                ((Number)row[3]).longValue(),
                ((Number)row[4]).longValue(),
                ((Number)row[5]).intValue() == 1,
                ((Number)row[6]).intValue() == 1,
                ((Number)row[7]).intValue(),
                ((Number)row[8]).intValue()
        )).toList();

        UserBasicInfoResponse userInfo = new UserBasicInfoResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getAge() == 0 ? null : (int) user.getAge(),
                user.getGroup() != null ? user.getGroup().getId() : -1
        );

        return new UserTestResultsWithInfoResponse(userInfo, questionResults);
    }

    @Override
    public GroupTestResultsResponse getGroupTestResults(int groupId) {
        List<UserModel> users = userRepository.findAllByGroup_Id(groupId);
        if (users == null || users.isEmpty()) {
            throw new IllegalArgumentException("No users found for this group");
        }

        List<UserTestResultsWithInfoResponse> userResults = users.stream()
                .map(user -> getUserTestResults(user.getId()))
                .toList();

        return new GroupTestResultsResponse(groupId, userResults);
    }
}