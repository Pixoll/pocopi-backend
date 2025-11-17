package com.pocopi.api.services;

import com.pocopi.api.dto.test.AssignedTestGroup;
import com.pocopi.api.dto.test.UserTestAttempt;
import com.pocopi.api.dto.test.UserTestAttemptAnswer;
import com.pocopi.api.exception.HttpException;
import com.pocopi.api.models.test.TestGroupModel;
import com.pocopi.api.models.test.UserTestAttemptModel;
import com.pocopi.api.models.user.UserModel;
import com.pocopi.api.repositories.ConfigRepository;
import com.pocopi.api.repositories.UserTestAttemptRepository;
import com.pocopi.api.repositories.projections.FormsCompletionStatusProjection;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class UserTestAttemptService {
    private final ConfigRepository configRepository;
    private final TestGroupService testGroupService;
    private final UserTestAttemptRepository userTestAttemptRepository;

    public UserTestAttemptService(
        ConfigRepository configRepository,
        TestGroupService testGroupService,
        UserTestAttemptRepository userTestAttemptRepository
    ) {
        this.configRepository = configRepository;
        this.testGroupService = testGroupService;
        this.userTestAttemptRepository = userTestAttemptRepository;
    }

    @Transactional
    public UserTestAttempt beginAttempt(UserModel user) {
        final int configVersion = configRepository.getLastConfig().getVersion();

        if (userTestAttemptRepository.hasUnfinishedAttempt(configVersion, user.getId())) {
            throw HttpException.conflict("User has already started an attempt");
        }

        final TestGroupModel group = testGroupService.sampleGroup();
        final AssignedTestGroup assignedGroup = testGroupService.getAssignedGroup(group);

        final UserTestAttemptModel newAttempt = UserTestAttemptModel.builder()
            .user(user)
            .group(group)
            .start(Instant.now())
            .build();

        userTestAttemptRepository.save(newAttempt);

        return new UserTestAttempt(
            false,
            false,
            false,
            List.of(),
            assignedGroup
        );
    }

    @Transactional
    public UserTestAttempt continueAttempt(int userId) {
        final int configVersion = configRepository.getLastConfig().getVersion();

        final UserTestAttemptModel unfinishedAttempt = userTestAttemptRepository
            .findUnfinishedAttempt(configVersion, userId)
            .orElseThrow(() -> HttpException.notFound("User has not started an attempt yet"));

        final AssignedTestGroup assignedGroup = testGroupService.getAssignedGroup(unfinishedAttempt.getGroup());

        final int totalQuestions = assignedGroup.phases().stream()
            .reduce(0, (subtotal, phase) -> subtotal + phase.questions().size(), Integer::sum);

        final FormsCompletionStatusProjection formsCompletionStatus = userTestAttemptRepository
            .getFormsCompletionStatus(unfinishedAttempt.getId());

        final List<UserTestAttemptAnswer> testAnswers = userTestAttemptRepository
            .getTestAnswers(unfinishedAttempt.getId())
            .stream().map(answer -> new UserTestAttemptAnswer(answer.getQuestionId(), answer.getOptionId()))
            .toList();

        final boolean completedTest = testAnswers.size() == totalQuestions;

        return new UserTestAttempt(
            formsCompletionStatus.getCompletedPreTestForm() == 1,
            completedTest,
            formsCompletionStatus.getCompletedPostTestForm() == 1,
            testAnswers,
            assignedGroup
        );
    }

    @Transactional
    public void discardAttempt(int userId) {
        final int configVersion = configRepository.getLastConfig().getVersion();

        final UserTestAttemptModel unfinishedAttempt = userTestAttemptRepository
            .findUnfinishedAttempt(configVersion, userId)
            .orElseThrow(() -> HttpException.notFound("User has not started an attempt yet"));

        userTestAttemptRepository.delete(unfinishedAttempt);
    }

    @Transactional
    public void endAttempt(int userId) {
        final int configVersion = configRepository.getLastConfig().getVersion();

        final UserTestAttemptModel unfinishedAttempt = userTestAttemptRepository
            .findUnfinishedAttempt(configVersion, userId)
            .orElseThrow(() -> HttpException.notFound("User has not started an attempt yet"));

        unfinishedAttempt.setEnd(Instant.now());
        userTestAttemptRepository.save(unfinishedAttempt);
    }
}
