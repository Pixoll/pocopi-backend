package com.pocopi.api.services;

import com.pocopi.api.dto.user.UserSummary;
import com.pocopi.api.dto.user.UsersSummary;
import com.pocopi.api.models.config.ConfigModel;
import com.pocopi.api.models.user.UserModel;
import com.pocopi.api.repositories.UserTestOptionLogRepository;
import com.pocopi.api.repositories.UserTestQuestionLogRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class SummaryService {
    private final UserService userService;
    private final ConfigService configService;
    private final UserTestQuestionLogRepository userTestQuestionLogRepository;
    private final UserTestOptionLogRepository userTestOptionLogRepository;

    public SummaryService(
        UserService userService,
        ConfigService configService,
        UserTestQuestionLogRepository userTestQuestionLogRepository,
        UserTestOptionLogRepository userTestOptionLogRepository
    ) {
        this.userService = userService;
        this.configService = configService;
        this.userTestQuestionLogRepository = userTestQuestionLogRepository;
        this.userTestOptionLogRepository = userTestOptionLogRepository;
    }

    public UserSummary getUserSummaryById(int userId) {
        return getUserSummary(userId);
    }

    public UsersSummary getAllUserSummaries() {
        final List<Integer> userIds = userService.getAllUserIds();
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

    private UserSummary getUserSummary(int userId) {
        final UserModel user = userService.getUserById(userId);
        final ConfigModel lastConfig = configService.findLastConfig();

        Long start = userTestQuestionLogRepository.findMostRecentlyStartTimeStamp(
            lastConfig.getVersion(),
            user.getId()
        );
        Long end = userTestQuestionLogRepository.findMostRecentlyEndTimeStamp(lastConfig.getVersion(), user.getId());
        //algo q diga q no respondio nada bien o quedo lol xd
        if (start == null || end == null) {
            start = 0L;
            end = 0L;
        }

        final List<Object[]> options = userTestOptionLogRepository.findAllLastOptionsByUserId(
            userId,
            lastConfig.getVersion()
        );
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
            user.getAge(),
            start,
            Math.toIntExact(end - start),
            questionsAnswered,
            questionsCorrect,
            percentage
        );
    }
}
