package com.pocopi.api.services;

import com.pocopi.api.dto.attempt.UserTestAttemptSummary;
import com.pocopi.api.dto.attempt.UsersTestAttemptsSummary;
import com.pocopi.api.exception.HttpException;
import com.pocopi.api.models.test.UserTestAttemptModel;
import com.pocopi.api.models.user.UserModel;
import com.pocopi.api.repositories.ConfigRepository;
import com.pocopi.api.repositories.UserRepository;
import com.pocopi.api.repositories.UserTestAttemptRepository;
import com.pocopi.api.repositories.UserTestOptionLogRepository;
import com.pocopi.api.repositories.projections.LastSelectedOptionProjection;
import com.pocopi.api.repositories.projections.LastSelectedOptionWithAttemptProjection;
import com.pocopi.api.repositories.projections.UserTestAttemptWithGroupProjection;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

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

    @Transactional
    public UsersTestAttemptsSummary getAllUsersTestAttemptsSummary() {
        final List<UserTestAttemptWithGroupProjection> testAttempts = userTestAttemptRepository.findFinishedAttempts();

        final ArrayList<Long> testAttemptIds = new ArrayList<>();
        final HashMap<Long, UserTestAttemptWithGroupProjection> testAttemptsById = new HashMap<>();

        for (final UserTestAttemptWithGroupProjection testAttempt : testAttempts) {
            testAttemptIds.add(testAttempt.getId());
            testAttemptsById.put(testAttempt.getId(), testAttempt);
        }

        final Map<Integer, UserModel> usersById = userRepository.findAllUsersByAttemptIds(testAttemptIds).stream()
            .collect(Collectors.toMap(UserModel::getId, (u) -> u, (a, b) -> b));

        final List<LastSelectedOptionWithAttemptProjection> lastSelectedOptions = userTestOptionLogRepository
            .findLastSelectedOptionsByAttemptIds(testAttemptIds);

        final HashMap<Long, TempUserTestAttemptSummary> tempSummariesByAttemptId = new HashMap<>();

        for (final LastSelectedOptionWithAttemptProjection lastSelectedOption : lastSelectedOptions) {
            final UserTestAttemptWithGroupProjection testAttempt = testAttemptsById
                .get(lastSelectedOption.getAttemptId());
            final UserModel user = usersById.get(lastSelectedOption.getUserId());

            if (!tempSummariesByAttemptId.containsKey(lastSelectedOption.getAttemptId())) {
                final TempUserTestAttemptSummary tempSummary = new TempUserTestAttemptSummary(testAttempt, user);
                tempSummary.processSelectedOption(lastSelectedOption);

                tempSummariesByAttemptId.put(lastSelectedOption.getAttemptId(), tempSummary);

                continue;
            }

            final TempUserTestAttemptSummary tempSummary = tempSummariesByAttemptId
                .get(lastSelectedOption.getAttemptId());

            tempSummary.processSelectedOption(lastSelectedOption);
        }

        final ArrayList<UserTestAttemptSummary> testSummaries = new ArrayList<>();

        int totalCorrect = 0;
        int totalTime = 0;
        int totalQuestionsAnswered = 0;

        for (final TempUserTestAttemptSummary tempSummary : tempSummariesByAttemptId.values()) {
            final UserTestAttemptSummary testAttemptSummary = tempSummary.toUserTestAttemptSummary();

            totalCorrect += testAttemptSummary.correctQuestions();
            totalQuestionsAnswered += testAttemptSummary.questionsAnswered();
            totalTime += testAttemptSummary.timeTaken();

            testSummaries.add(testAttemptSummary);
        }

        return new UsersTestAttemptsSummary(
            (double) totalCorrect / totalQuestionsAnswered,
            (double) totalTime / totalQuestionsAnswered,
            totalQuestionsAnswered,
            testSummaries
        );
    }

    @Transactional
    public UserTestAttemptSummary getUserTestAttemptSummary(int userId) {
        final UserModel user = userRepository.getUserByUserId(userId);
        final int configVersion = configRepository.getLastConfig().getVersion();

        final UserTestAttemptModel attempt = userTestAttemptRepository
            .findLatestFinishedAttempt(configVersion, userId)
            .orElseThrow(() ->
                HttpException.notFound("User with id " + userId + " does not have any completed test attempts")
            );

        final long start = attempt.getStart().toEpochMilli();
        final long end = attempt.getEnd().toEpochMilli();

        final List<LastSelectedOptionProjection> lastSelectedOptions = userTestOptionLogRepository
            .findLastSelectedOptionsByAttemptId(attempt.getId());

        int questionsAnswered = 0;
        int correctQuestions = 0;

        final Set<Integer> countedQuestions = new HashSet<>();

        for (final LastSelectedOptionProjection lastSelectedOption : lastSelectedOptions) {
            final int questionId = lastSelectedOption.getQuestionId();

            if (!countedQuestions.contains(questionId)) {
                countedQuestions.add(questionId);
                questionsAnswered++;
                if (lastSelectedOption.getCorrect()) {
                    correctQuestions++;
                }
            }
        }

        final double percentage = questionsAnswered > 0 ? ((double) correctQuestions / questionsAnswered) * 100 : 0.0;

        return new UserTestAttemptSummary(
            user.getId(),
            user.getUsername(),
            user.getName(),
            user.getEmail(),
            user.getAge() != null ? user.getAge().intValue() : null,
            attempt.getGroup().getConfig().getVersion(),
            attempt.getGroup().getLabel(),
            start,
            Math.toIntExact(end - start),
            correctQuestions,
            questionsAnswered,
            percentage
        );
    }

    private static class TempUserTestAttemptSummary {
        private final UserTestAttemptWithGroupProjection attempt;
        private final UserModel user;
        private int correctQuestions = 0;
        private int questionsAnswered = 0;

        private final Set<Integer> countedQuestions = new HashSet<>();

        public TempUserTestAttemptSummary(UserTestAttemptWithGroupProjection attempt, UserModel user) {
            this.attempt = attempt;
            this.user = user;
        }

        public void processSelectedOption(LastSelectedOptionWithAttemptProjection selectedOption) {
            if (countedQuestions.contains(selectedOption.getQuestionId())) {
                return;
            }

            countedQuestions.add(selectedOption.getQuestionId());
            questionsAnswered++;
            if (selectedOption.getCorrect()) {
                correctQuestions++;
            }
        }

        public UserTestAttemptSummary toUserTestAttemptSummary() {
            final long start = attempt.getStart();
            final long end = attempt.getEnd();
            final double percentage = questionsAnswered > 0
                ? ((double) correctQuestions / questionsAnswered) * 100
                : 0.0;

            return new UserTestAttemptSummary(
                user.getId(),
                user.getUsername(),
                user.getName(),
                user.getEmail(),
                user.getAge() != null ? user.getAge().intValue() : null,
                attempt.getConfigVersion(),
                attempt.getGroup(),
                start,
                Math.toIntExact(end - start),
                correctQuestions,
                questionsAnswered,
                percentage
            );
        }
    }
}
