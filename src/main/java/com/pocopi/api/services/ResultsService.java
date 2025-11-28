package com.pocopi.api.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pocopi.api.dto.event.OptionEventLog;
import com.pocopi.api.dto.event.OptionSelectionEvent;
import com.pocopi.api.dto.event.QuestionEventLog;
import com.pocopi.api.dto.event.QuestionTimestamp;
import com.pocopi.api.dto.results.*;
import com.pocopi.api.dto.user.User;
import com.pocopi.api.exception.HttpException;
import com.pocopi.api.models.test.TestOptionEventType;
import com.pocopi.api.models.user.UserModel;
import com.pocopi.api.repositories.UserRepository;
import com.pocopi.api.repositories.UserTestAttemptRepository;
import com.pocopi.api.repositories.UserTestOptionLogRepository;
import com.pocopi.api.repositories.UserTestQuestionLogRepository;
import com.pocopi.api.repositories.projections.OptionEventProjection;
import com.pocopi.api.repositories.projections.QuestionEventProjection;
import com.pocopi.api.repositories.projections.UserTestAttemptWithGroupProjection;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
public class ResultsService {
    private final static ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final UserRepository userRepository;
    private final FormAnswerService formAnswerService;
    private final UserTestQuestionLogRepository userTestQuestionLogRepository;
    private final UserTestOptionLogRepository userTestOptionLogRepository;
    private final UserTestAttemptRepository userTestAttemptRepository;

    public ResultsService(
        UserRepository userRepository,
        FormAnswerService formAnswerService,
        UserTestQuestionLogRepository userTestQuestionLogRepository,
        UserTestOptionLogRepository userTestOptionLogRepository,
        UserTestAttemptRepository userTestAttemptRepository
    ) {
        this.userRepository = userRepository;
        this.formAnswerService = formAnswerService;
        this.userTestQuestionLogRepository = userTestQuestionLogRepository;
        this.userTestOptionLogRepository = userTestOptionLogRepository;
        this.userTestAttemptRepository = userTestAttemptRepository;
    }

    @Transactional
    public List<ResultsByUser> getAllResults() {
        return userRepository.findAll().stream()
            .map(user -> getUserResults(user.getId()))
            .toList();
    }

    @Transactional
    public List<FormAnswersByUser> getAllFormResults() {
        return userRepository.findAll().stream()
            .map(user -> getUserFormResults(user.getId()))
            .toList();
    }

    @Transactional
    public List<TestResultsByUser> getAllTestResults() {
        return userRepository.findAll().stream()
            .map(user -> getUserTestResults(user.getId()))
            .toList();
    }

    @Transactional
    public ResultsByUser getUserResults(int userId) {
        final UserModel user = userRepository.findById(userId)
            .orElseThrow(() -> HttpException.notFound("User " + userId + " not found"));

        final User userInfo = new User(
            user.getId(),
            user.getUsername(),
            user.isAnonymous(),
            user.getName(),
            user.getEmail(),
            user.getAge() != null ? user.getAge().intValue() : null
        );

        final List<FormAnswersByConfig> formAnswers = getUserFormResults(userId).answers();
        final List<TestResultsByConfig> testResults = getUserTestResults(userId).results();

        final HashMap<Integer, ResultsByConfig> resultsByConfigMap = new HashMap<>();

        for (final FormAnswersByConfig formAnswer : formAnswers) {
            resultsByConfigMap.putIfAbsent(
                formAnswer.configVersion(),
                new ResultsByConfig(formAnswer.configVersion(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>())
            );

            final ResultsByConfig resultsByConfig = resultsByConfigMap.get(formAnswer.configVersion());

            resultsByConfig.preTestForm().addAll(formAnswer.preTestForm());
            resultsByConfig.postTestForm().addAll(formAnswer.postTestForm());
        }

        for (final TestResultsByConfig testResult : testResults) {
            resultsByConfigMap.putIfAbsent(
                testResult.configVersion(),
                new ResultsByConfig(testResult.configVersion(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>())
            );

            final ResultsByConfig resultsByConfig = resultsByConfigMap.get(testResult.configVersion());

            resultsByConfig.attemptsResults().addAll(testResult.attemptsResults());
        }

        return new ResultsByUser(userInfo, resultsByConfigMap.values().stream().toList());
    }

    @Transactional
    public FormAnswersByUser getUserFormResults(int userId) {
        final UserModel user = userRepository.findById(userId)
            .orElseThrow(() -> HttpException.notFound("User " + userId + " not found"));

        final List<FormAnswersByConfig> formAnswers = formAnswerService.getUserFormAnswers(user.getId());

        final User userInfo = new User(
            user.getId(),
            user.getUsername(),
            user.isAnonymous(),
            user.getName(),
            user.getEmail(),
            user.getAge() != null ? user.getAge().intValue() : null
        );

        return new FormAnswersByUser(userInfo, formAnswers);
    }

    @Transactional
    public TestResultsByUser getUserTestResults(int userId) {
        final UserModel user = userRepository.findById(userId)
            .orElseThrow(() -> HttpException.notFound("User " + userId + " not found"));

        final List<UserTestAttemptWithGroupProjection> testAttempts
            = userTestAttemptRepository.findFinishedAttemptsByUserId(userId);

        final List<Long> attemptIds = testAttempts.stream().map(UserTestAttemptWithGroupProjection::getId).toList();

        final List<QuestionEventProjection> questionEvents
            = userTestQuestionLogRepository.findAllQuestionEventsByAttemptIds(attemptIds);
        final List<OptionEventProjection> optionEvents = userTestOptionLogRepository.findAllOptionEventsByAttemptIds(
            attemptIds);

        // configVersion -> attemptId
        final HashMap<Integer, HashMap<Long, TempTestResult>> groupedTempTestResults = new HashMap<>();

        for (final UserTestAttemptWithGroupProjection testAttempt : testAttempts) {
            groupedTempTestResults.putIfAbsent(testAttempt.getConfigVersion(), new HashMap<>());

            final HashMap<Long, TempTestResult> tempTestResultsByAttemptId
                = groupedTempTestResults.get(testAttempt.getConfigVersion());

            tempTestResultsByAttemptId.put(testAttempt.getId(), new TempTestResult(testAttempt));
        }

        for (final QuestionEventProjection questionEvent : questionEvents) {
            final HashMap<Long, TempTestResult> tempTestResultsByAttemptId
                = groupedTempTestResults.get(questionEvent.getConfigVersion());

            final TempTestResult tempTestResult = tempTestResultsByAttemptId.get(questionEvent.getAttemptId());

            tempTestResult.processQuestionEvent(questionEvent);
        }

        for (final OptionEventProjection optionEvent : optionEvents) {
            final HashMap<Long, TempTestResult> tempTestResultsByAttemptId
                = groupedTempTestResults.get(optionEvent.getConfigVersion());

            final TempTestResult tempTestResult = tempTestResultsByAttemptId.get(optionEvent.getAttemptId());

            tempTestResult.processOptionEvent(optionEvent);
        }

        final User userInfo = new User(
            user.getId(),
            user.getUsername(),
            user.isAnonymous(),
            user.getName(),
            user.getEmail(),
            user.getAge() != null ? user.getAge().intValue() : null
        );

        final List<TestResultsByConfig> results = groupedTempTestResults.entrySet()
            .stream()
            .map(configEntry -> new TestResultsByConfig(
                configEntry.getKey(),
                configEntry.getValue().values().stream().map(TempTestResult::toTestResult).toList()
            ))
            .toList();

        return new TestResultsByUser(userInfo, results);
    }

    private static List<QuestionTimestamp> parseJsonTimestampArray(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }

        try {
            return OBJECT_MAPPER.readValue(
                json, new TypeReference<>() {
                }
            );
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse timestamps JSON", e);
        }
    }

    private static class TempTestResult {
        private final UserTestAttemptWithGroupProjection attempt;
        private final HashMap<Integer, QuestionEventLog> questionEventLogsMap = new HashMap<>();
        private int correctQuestions = 0;
        private int questionsAnswered = 0;

        public TempTestResult(UserTestAttemptWithGroupProjection attempt) {
            this.attempt = attempt;
        }

        public void processQuestionEvent(QuestionEventProjection questionEvent) {
            if (questionEventLogsMap.containsKey(questionEvent.getQuestionId())) {
                return;
            }

            final QuestionEventLog questionEventLog = new QuestionEventLog(
                questionEvent.getQuestionId(),
                parseJsonTimestampArray(questionEvent.getTimestampsJson()),
                questionEvent.getCorrect() == 1,
                questionEvent.getSkipped() == 1,
                questionEvent.getTotalOptionChanges().intValue(),
                questionEvent.getTotalOptionHovers().intValue(),
                new ArrayList<>(),
                new ArrayList<>()
            );

            if (!questionEventLog.skipped()) {
                questionsAnswered++;
            }

            if (questionEventLog.correct()) {
                correctQuestions++;
            }

            questionEventLogsMap.put(questionEvent.getQuestionId(), questionEventLog);
        }

        public void processOptionEvent(OptionEventProjection optionEvent) {
            if (questionEventLogsMap.containsKey(optionEvent.getQuestionId())) {
                return;
            }

            final OptionEventLog eventLog = new OptionEventLog(
                optionEvent.getOptionId(),
                TestOptionEventType.fromValue(optionEvent.getType()),
                optionEvent.getTimestamp()
            );

            final QuestionEventLog questionEventLog = questionEventLogsMap.get(optionEvent.getQuestionId());

            questionEventLog.events().add(eventLog);

            if (eventLog.type() == TestOptionEventType.SELECT) {
                final OptionSelectionEvent selectionEvent = new OptionSelectionEvent(
                    eventLog.optionId(),
                    eventLog.timestamp()
                );

                questionEventLog.optionSelections().add(selectionEvent);
            }
        }

        public TestResult toTestResult() {
            final long start = attempt.getStart();
            final long end = attempt.getEnd();
            final double percentage = questionsAnswered > 0
                ? ((double) correctQuestions / questionsAnswered) * 100
                : 0.0;

            return new TestResult(
                attempt.getId(),
                attempt.getGroup(),
                start,
                Math.toIntExact(end - start),
                correctQuestions,
                questionsAnswered,
                percentage,
                questionEventLogsMap.values().stream().toList()
            );
        }
    }
}
