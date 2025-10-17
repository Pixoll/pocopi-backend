package com.pocopi.api.services.implementations;

import com.pocopi.api.dto.Results.UserAllResultsResponse;
import com.pocopi.api.dto.Results.GroupFullResultsResponse;
import com.pocopi.api.dto.FormResult.UserFormWithInfoResultsResponse;
import com.pocopi.api.dto.FormResult.FormAnswers;
import com.pocopi.api.dto.TestResult.UserTestResultsWithInfoResponse;
import com.pocopi.api.dto.TestResult.TestQuestionResult;
import com.pocopi.api.dto.Results.UserBasicInfoResponse;
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
    public UserAllResultsResponse getUserAllResults(int userId) {
        UserModel user = userRepository.getUserByUserId(userId);
        if (user == null) throw new IllegalArgumentException("Usuario no encontrado");

        // Info usuario
        UserBasicInfoResponse userInfo = new UserBasicInfoResponse(
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getAge() == 0 ? null : (int) user.getAge()
        );

        // Formularios
        UserFormWithInfoResultsResponse forms = formResultsService.getUserFormResults(userId);
        List<FormAnswers> pre = forms.pre();
        List<FormAnswers> post = forms.post();

        // Tests
        UserTestResultsWithInfoResponse tests = testResultsService.getUserTestResults(userId);
        List<TestQuestionResult> questions = tests.questions();

        return new UserAllResultsResponse(
            userInfo,
            new UserAllResultsResponse.UserFormsPart(pre, post),
            new UserAllResultsResponse.UserTestsPart(questions)
        );
    }

    @Override
    public GroupFullResultsResponse getGroupFullResults(int groupId) {
        List<UserModel> users = userRepository.getAllUsers(); // TODO changed from getByGroupId
        if (users == null || users.isEmpty()) {
            throw new IllegalArgumentException("No users found for this group");
        }

        List<UserAllResultsResponse> userResults = users.stream()
            .map(user -> getUserAllResults(user.getId()))
            .toList();

        return new GroupFullResultsResponse(groupId, userResults);
    }
}
