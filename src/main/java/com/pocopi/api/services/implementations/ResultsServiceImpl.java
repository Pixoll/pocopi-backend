package com.pocopi.api.services.implementations;

import com.pocopi.api.dto.Results.UserFullResultsResponse;
import com.pocopi.api.dto.Results.GroupFullResultsResponse;
import com.pocopi.api.dto.FormResult.UserFormWithInfoResultsResponse;
import com.pocopi.api.dto.TestResult.UserTestResultsWithInfoResponse;
import com.pocopi.api.services.interfaces.FormResultsService;
import com.pocopi.api.services.interfaces.TestResultsService;
import com.pocopi.api.services.interfaces.ResultsService;
import com.pocopi.api.repositories.UserRepository;
import com.pocopi.api.models.user.UserModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ResultsServiceImpl implements ResultsService {
    private final FormResultsService formResultsService;
    private final TestResultsService testResultsService;
    private final UserRepository userRepository;

    @Autowired
    public ResultsServiceImpl(
            FormResultsService formResultsService,
            TestResultsService testResultsService,
            UserRepository userRepository
    ) {
        this.formResultsService = formResultsService;
        this.testResultsService = testResultsService;
        this.userRepository = userRepository;
    }

    @Override
    public UserFullResultsResponse getUserFullResults(int userId) {
        UserFormWithInfoResultsResponse forms = formResultsService.getUserFormResults(userId);
        UserTestResultsWithInfoResponse tests = testResultsService.getUserTestResults(userId);
        return new UserFullResultsResponse(forms, tests);
    }

    @Override
    public GroupFullResultsResponse getGroupFullResults(int groupId) {
        List<UserModel> users = userRepository.findAllByGroup_Id(groupId);
        if (users == null || users.isEmpty()) {
            throw new IllegalArgumentException("No users found for this group");
        }

        List<UserFullResultsResponse> userResults = users.stream()
                .map(user -> getUserFullResults(user.getId()))
                .toList();

        return new GroupFullResultsResponse(groupId, userResults);
    }
}