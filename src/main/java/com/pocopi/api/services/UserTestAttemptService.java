package com.pocopi.api.services;

import com.pocopi.api.dto.test.AssignedTestGroup;
import com.pocopi.api.dto.test.UserTestAttempt;
import com.pocopi.api.exception.HttpException;
import com.pocopi.api.models.test.TestGroupModel;
import com.pocopi.api.models.test.UserTestAttemptModel;
import com.pocopi.api.models.user.UserModel;
import com.pocopi.api.repositories.ConfigRepository;
import com.pocopi.api.repositories.UserTestAttemptRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

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
        final int configVersion = configRepository.findLastConfig().getVersion();

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

        return new UserTestAttempt(assignedGroup);
    }

    @Transactional
    public UserTestAttempt continueAttempt(int userId) {
        final int configVersion = configRepository.findLastConfig().getVersion();

        final UserTestAttemptModel unfinishedAttempt = userTestAttemptRepository
            .findUnfinishedAttempt(configVersion, userId)
            .orElseThrow(() -> HttpException.notFound("User has not started an attempt yet"));

        final AssignedTestGroup assignedGroup = testGroupService.getAssignedGroup(unfinishedAttempt.getGroup());

        return new UserTestAttempt(assignedGroup);
    }

    @Transactional
    public void discardAttempt(int userId) {
        final int configVersion = configRepository.findLastConfig().getVersion();

        final UserTestAttemptModel unfinishedAttempt = userTestAttemptRepository
            .findUnfinishedAttempt(configVersion, userId)
            .orElseThrow(() -> HttpException.notFound("User has not started an attempt yet"));

        userTestAttemptRepository.delete(unfinishedAttempt);
    }

    @Transactional
    public void endAttempt(int userId) {
        final int configVersion = configRepository.findLastConfig().getVersion();

        final UserTestAttemptModel unfinishedAttempt = userTestAttemptRepository
            .findUnfinishedAttempt(configVersion, userId)
            .orElseThrow(() -> HttpException.notFound("User has not started an attempt yet"));

        unfinishedAttempt.setEnd(Instant.now());
        userTestAttemptRepository.save(unfinishedAttempt);
    }
}
