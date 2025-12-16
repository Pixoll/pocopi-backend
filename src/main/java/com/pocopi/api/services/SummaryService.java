package com.pocopi.api.services;

import com.pocopi.api.dto.attempt.TestAttemptSummary;
import com.pocopi.api.dto.attempt.TestAttemptsSummary;
import com.pocopi.api.dto.user.User;
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
    public TestAttemptsSummary getAllTestAttemptsSummary() {
        final List<UserTestAttemptWithGroupProjection> testAttempts = userTestAttemptRepository.findFinishedAttempts();

        final ArrayList<Long> testAttemptIds = testAttempts.stream()
            .map(UserTestAttemptWithGroupProjection::getId)
            .collect(Collectors.toCollection(ArrayList::new));

        final Map<Integer, UserModel> usersById = userRepository.findAllUsersByAttemptIds(testAttemptIds).stream()
            .collect(Collectors.toMap(UserModel::getId, (u) -> u, (a, b) -> b));

        final TreeMap<Long, TempTestAttemptSummary> tempSummariesByAttemptId = new TreeMap<>();

        for (final UserTestAttemptWithGroupProjection testAttempt : testAttempts) {
            final UserModel user = usersById.get(testAttempt.getUserId());
            final TempTestAttemptSummary tempSummary = new TempTestAttemptSummary(testAttempt, user);
            tempSummariesByAttemptId.put(testAttempt.getId(), tempSummary);
        }

        final List<LastSelectedOptionWithAttemptProjection> lastSelectedOptions = userTestOptionLogRepository
            .findLastSelectedOptionsByAttemptIds(testAttemptIds);

        for (final LastSelectedOptionWithAttemptProjection lastSelectedOption : lastSelectedOptions) {
            final TempTestAttemptSummary tempSummary = tempSummariesByAttemptId.get(lastSelectedOption.getAttemptId());
            tempSummary.processSelectedOption(lastSelectedOption);
        }

        final ArrayList<TestAttemptSummary> testSummaries = new ArrayList<>();

        int totalCorrect = 0;
        int totalTime = 0;
        int totalQuestionsAnswered = 0;

        for (final TempTestAttemptSummary tempSummary : tempSummariesByAttemptId.values()) {
            final TestAttemptSummary testAttemptSummary = tempSummary.toTestAttemptSummary();

            totalCorrect += testAttemptSummary.correctQuestions();
            totalQuestionsAnswered += testAttemptSummary.questionsAnswered();
            totalTime += testAttemptSummary.timeTaken();

            testSummaries.add(testAttemptSummary);
        }

        testSummaries.sort(Comparator.comparing(TestAttemptSummary::timestamp).reversed());

        final double averageAccuracy = totalQuestionsAnswered > 0
            ? (double) totalCorrect / totalQuestionsAnswered
            : 0.0;
        final double averageTimeTaken = totalQuestionsAnswered > 0 ? (double) totalTime / totalQuestionsAnswered : 0.0;

        return new TestAttemptsSummary(
            averageAccuracy,
            averageTimeTaken,
            totalQuestionsAnswered,
            testSummaries
        );
    }

    @Transactional
    public TestAttemptSummary getUserLatestTestAttemptSummary(int userId) {
        final UserModel user = userRepository.findById(userId)
            .orElseThrow(() -> HttpException.notFound("User " + userId + " not found"));

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

        final User userInfo = new User(
            user.getId(),
            user.getUsername(),
            user.isAnonymous(),
            user.getName(),
            user.getEmail(),
            user.getAge() != null ? user.getAge().intValue() : null
        );

        return new TestAttemptSummary(
            userInfo,
            attempt.getGroup().getConfig().getVersion(),
            attempt.getGroup().getLabel(),
            start,
            Math.toIntExact(end - start),
            correctQuestions,
            questionsAnswered,
            percentage
        );
    }

    private static class TempTestAttemptSummary {
        private final UserTestAttemptWithGroupProjection attempt;
        private final UserModel user;
        private int correctQuestions = 0;
        private int questionsAnswered = 0;

        private final Set<Integer> countedQuestions = new HashSet<>();

        public TempTestAttemptSummary(UserTestAttemptWithGroupProjection attempt, UserModel user) {
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

        public TestAttemptSummary toTestAttemptSummary() {
            final User userInfo = new User(
                user.getId(),
                user.getUsername(),
                user.isAnonymous(),
                user.getName(),
                user.getEmail(),
                user.getAge() != null ? user.getAge().intValue() : null
            );

            final long start = attempt.getStart();
            final long end = attempt.getEnd();
            final double percentage = questionsAnswered > 0
                ? ((double) correctQuestions / questionsAnswered) * 100
                : 0.0;

            return new TestAttemptSummary(
                userInfo,
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
