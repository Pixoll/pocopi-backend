package com.pocopi.api.services.implementations;

import com.pocopi.api.dto.User.TotalUserSummaryResponse;
import com.pocopi.api.dto.User.UserSummaryResponse;
import com.pocopi.api.models.ConfigModel;
import com.pocopi.api.models.UserModel;
import com.pocopi.api.repositories.UserTestOptionLogRepository;
import com.pocopi.api.repositories.UserTestQuestionLogRepository;
import com.pocopi.api.services.interfaces.ConfigService;
import com.pocopi.api.services.interfaces.SummaryService;
import com.pocopi.api.services.interfaces.UserService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class SummaryServiceImp implements SummaryService {
    private final UserService userService;
    private final ConfigService configService;
    private final UserTestQuestionLogRepository userTestQuestionLogRepository;
    private final UserTestOptionLogRepository userTestOptionLogRepository;

    public SummaryServiceImp(UserService userService,
                             ConfigService configService,
                             UserTestQuestionLogRepository userTestQuestionLogRepository,
                             UserTestOptionLogRepository userTestOptionLogRepository
    ) {
        this.userService = userService;
        this.configService = configService;
        this.userTestQuestionLogRepository = userTestQuestionLogRepository;
        this.userTestOptionLogRepository = userTestOptionLogRepository;
    }
    @Override
    public UserSummaryResponse getUserSummaryById(int userId) {
        return getUserSummary(userId);
    }

    @Override
    public TotalUserSummaryResponse getAllUserSummaries() {
        List<Integer> userIds = userService.getAllUserIds();
        List<UserSummaryResponse>  userSummaries = new ArrayList<>();
        double totalCorrect = 0;
        int totalTime = 0;
        int totalQuestionsAnswered = 0;

        for (Integer userId : userIds) {
            UserSummaryResponse userSummaryResponse = getUserSummary(userId);
            totalCorrect += userSummaryResponse.correctQuestions();
            totalQuestionsAnswered += userSummaryResponse.questionsAnswered();
            totalTime += userSummaryResponse.timeTaken();
            userSummaries.add(getUserSummary(userId));

        }
        return new TotalUserSummaryResponse(
            totalCorrect / totalQuestionsAnswered,
            (double) totalTime /totalQuestionsAnswered,
            totalQuestionsAnswered,
            userSummaries
        );
    }
    private UserSummaryResponse getUserSummary(int userId){
        UserModel user = userService.getUserById(userId);
        ConfigModel lastConfig = configService.findLastConfig();

        Long start = userTestQuestionLogRepository.findMostRecentlyStartTimeStamp(lastConfig.getVersion(), user.getId());
        Long end = userTestQuestionLogRepository.findMostRecentlyEndTimeStamp(lastConfig.getVersion(), user.getId());
        //algo q diga q no respondio nada bien o quedo lol xd
        if (start == null || end == null) {
            start = 0L;
            end = 0L;
        }

        List<Object[]> options = userTestOptionLogRepository.findAllLastOptionsByUserId(userId, lastConfig.getVersion());
        int questionsAnswered = 0;
        int questionsCorrect = 0;

        Set<Integer> countedQuestions = new HashSet<>();

        for (Object[] row : options) {
            Integer questionId = ((Number) row[0]).intValue();
            Boolean correct = (Boolean) row[3];

            if (!countedQuestions.contains(questionId)) {
                countedQuestions.add(questionId);
                questionsAnswered++;
                if (Boolean.TRUE.equals(correct)) {
                    questionsCorrect++;
                }
            }
        }

        double percentage = questionsAnswered > 0 ? ((double) questionsCorrect / questionsAnswered) * 100 : 0.0;

        return new UserSummaryResponse(
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getAge(),
            user.getGroup().getLabel(),
            start,
            Math.toIntExact(end - start),
            questionsAnswered,
            questionsCorrect,
            percentage
        );
    }
}
