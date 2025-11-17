package com.pocopi.api.services;

import com.pocopi.api.dto.user.UserSummary;
import com.pocopi.api.dto.user.UsersSummary;
import com.pocopi.api.exception.HttpException;
import com.pocopi.api.models.test.UserTestAttemptModel;
import com.pocopi.api.models.user.UserModel;
import com.pocopi.api.repositories.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class SummaryService {
    private final ConfigRepository configRepository;
    private final UserTestOptionLogRepository userTestOptionLogRepository;
    private final UserRepository userRepository;
    private final UserTestAttemptRepository userTestAttemptRepository;

    public SummaryService(
        ConfigRepository configRepository,
        UserTestOptionLogRepository userTestOptionLogRepository,
        UserRepository userRepository,
        UserTestAttemptRepository userTestAttemptRepository
    ) {
        this.configRepository = configRepository;
        this.userTestOptionLogRepository = userTestOptionLogRepository;
        this.userRepository = userRepository;
        this.userTestAttemptRepository = userTestAttemptRepository;
    }

    public UsersSummary getAllUserSummaries() {
        final List<Integer> userIds = userRepository.getAllUserIds();
        final List<UserSummary> userSummaries = new ArrayList<>();
        double totalCorrect = 0;
        int totalTime = 0;
        int totalQuestionsAnswered = 0;

        for (final Integer userId : userIds) {
            final UserSummary userSummaryResponse = getUserSummary(userId);
            totalCorrect += userSummaryResponse.correctQuestions();
            totalQuestionsAnswered += userSummaryResponse.questionsAnswered();
            totalTime += userSummaryResponse.timeTaken();
            userSummaries.add(getUserSummary(userId));
        }
        return new UsersSummary(
            totalCorrect / totalQuestionsAnswered,
            (double) totalTime / totalQuestionsAnswered,
            totalQuestionsAnswered,
            userSummaries
        );
    }

    public UserSummary getUserSummary(int userId) {
        final UserModel user = userRepository.getUserByUserId(userId);
        final int configVersion = configRepository.getLastConfig().getVersion();

        final UserTestAttemptModel attempt = userTestAttemptRepository
            .findLatestFinishedAttempt(configVersion, userId)
            .orElseThrow(() -> HttpException.notFound("User with id " + userId + " does not have any test attempts"));

        final long start = attempt.getStart().toEpochMilli();
        final long end = attempt.getEnd().toEpochMilli();

        final List<Object[]> options = userTestOptionLogRepository.findAllLastOptionsByUserId(userId, configVersion);
        int questionsAnswered = 0;
        int questionsCorrect = 0;

        final Set<Integer> countedQuestions = new HashSet<>();

        for (final Object[] row : options) {
            final Integer questionId = ((Number) row[0]).intValue();
            final Boolean correct = (Boolean) row[3];

            if (!countedQuestions.contains(questionId)) {
                countedQuestions.add(questionId);
                questionsAnswered++;
                if (Boolean.TRUE.equals(correct)) {
                    questionsCorrect++;
                }
            }
        }

        final double percentage = questionsAnswered > 0 ? ((double) questionsCorrect / questionsAnswered) * 100 : 0.0;

        return new UserSummary(
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getAge() != null ? user.getAge().intValue() : null,
            start,
            Math.toIntExact(end - start),
            questionsAnswered,
            questionsCorrect,
            percentage
        );
    }
}
