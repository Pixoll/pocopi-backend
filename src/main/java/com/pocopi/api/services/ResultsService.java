package com.pocopi.api.services;

import com.pocopi.api.dto.form.FormAnswers;
import com.pocopi.api.dto.results.*;
import com.pocopi.api.dto.user.UserBasicInfo;
import com.pocopi.api.models.user.UserModel;
import com.pocopi.api.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ResultsService {
    private final FormResultsService formResultsService;
    private final TestResultsService testResultsService;
    private final UserRepository userRepository;

    public ResultsService(
        FormResultsService formResultsService,
        TestResultsService testResultsService,
        UserRepository userRepository
    ) {
        this.formResultsService = formResultsService;
        this.testResultsService = testResultsService;
        this.userRepository = userRepository;
    }

    public ResultsByUser getUserAllResults(int userId) {
        final UserModel user = userRepository.getUserByUserId(userId);
        if (user == null) {
            throw new IllegalArgumentException("Usuario no encontrado");
        }

        // Info usuario
        final UserBasicInfo userInfo = new UserBasicInfo(
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getAge() == 0 ? null : (int) user.getAge()
        );

        // Formularios
        final FormAnswersByUser forms = formResultsService.getUserFormResults(userId);
        final List<FormAnswers> pre = forms.pre();
        final List<FormAnswers> post = forms.post();

        // Tests
        final TestResultByUser tests = testResultsService.getUserTestResults(userId);
        final List<TestQuestionResult> questions = tests.questions();

        return new ResultsByUser(
            userInfo,
            new ResultsByUser.UserFormsResult(pre, post),
            new ResultsByUser.UserTestsResult(questions)
        );
    }

    public ResultsByGroup getGroupFullResults(int groupId) {
        final List<UserModel> users = userRepository.getAllUsers(); // TODO changed from getByGroupId
        if (users == null || users.isEmpty()) {
            throw new IllegalArgumentException("No users found for this group");
        }

        final List<ResultsByUser> userResults = users.stream()
            .map(user -> getUserAllResults(user.getId()))
            .toList();

        return new ResultsByGroup(groupId, userResults);
    }
}
