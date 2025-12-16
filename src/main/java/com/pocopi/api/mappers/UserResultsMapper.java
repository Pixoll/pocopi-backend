package com.pocopi.api.mappers;

import com.pocopi.api.dto.csv.ResultCsv;
import com.pocopi.api.dto.csv.ResultCsvType;
import com.pocopi.api.dto.csv.UserFormResultCsvRow;
import com.pocopi.api.dto.csv.UserTestResultCsvRow;
import com.pocopi.api.dto.event.OptionEventLog;
import com.pocopi.api.dto.event.QuestionEventLog;
import com.pocopi.api.dto.event.QuestionTimestamp;
import com.pocopi.api.dto.form.FormAnswer;
import com.pocopi.api.dto.results.*;
import com.pocopi.api.dto.user.User;
import com.pocopi.api.models.form.FormType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class UserResultsMapper {
    private static final String CSV_DELIMITER = ";";

    public List<ResultCsv> userResultsToCsv(ResultsByUser userResults) {
        final FormSubmissionsByUser userFormResults = new FormSubmissionsByUser(
            userResults.user(),
            userResults.results().stream()
                .map(resultsByConfig -> new FormSubmissionsByConfig(
                    resultsByConfig.configVersion(),
                    resultsByConfig.preTestForm(),
                    resultsByConfig.postTestForm()
                ))
                .toList()
        );

        final TestResultsByUser userTestResults = new TestResultsByUser(
            userResults.user(),
            userResults.results().stream()
                .map(resultsByConfig -> new TestResultsByConfig(
                    resultsByConfig.configVersion(),
                    resultsByConfig.attemptsResults()
                ))
                .toList()
        );

        final String username = userResults.user().username();
        final String userFormResultsCsv = userFormResultsToCsv(userFormResults);
        final String userTestResultsCsv = userTestResultsToCsv(userTestResults);

        return List.of(
            new ResultCsv(ResultCsvType.FORMS, username, userFormResultsCsv),
            new ResultCsv(ResultCsvType.TEST, username, userTestResultsCsv)
        );
    }

    public String userFormResultsToCsv(FormSubmissionsByUser userFormResults) {
        final ArrayList<UserFormResultCsvRow> rows = new ArrayList<>();

        rows.add(new UserFormResultCsvRow(
            "user_id",
            "user_username",
            "user_anonymous",
            "user_name",
            "user_email",
            "user_age",
            "config_version",
            "attempt_id",
            "form_type",
            "timestamp",
            "question_id",
            "option_id",
            "value",
            "answer"
        ));

        final User user = userFormResults.user();

        for (final FormSubmissionsByConfig resultsByConfig : userFormResults.submissions()) {
            Map.of(FormType.PRE, resultsByConfig.preTestForm(), FormType.POST, resultsByConfig.postTestForm())
                .forEach((type, formAnswers) -> {
                    for (final FormSubmission formSubmission : resultsByConfig.preTestForm()) {
                        for (final FormAnswer formAnswer : formSubmission.answers()) {
                            rows.add(new UserFormResultCsvRow(
                                Integer.toString(user.id()),
                                user.username(),
                                Boolean.toString(user.anonymous()),
                                user.name(),
                                user.email(),
                                user.age() != null ? Integer.toString(user.age()) : null,
                                Integer.toString(resultsByConfig.configVersion()),
                                Long.toString(formSubmission.attemptId()),
                                type.getName(),
                                Long.toString(formSubmission.timestamp()),
                                Integer.toString(formAnswer.questionId()),
                                formAnswer.optionId() != null ? Integer.toString(formAnswer.optionId()) : null,
                                formAnswer.value() != null ? Integer.toString(formAnswer.value()) : null,
                                formAnswer.answer()
                            ));
                        }
                    }
                });
        }

        return rows.stream().map(r -> r.toString(CSV_DELIMITER)).collect(Collectors.joining("\n"));
    }

    public String userTestResultsToCsv(TestResultsByUser userTestResults) {
        final ArrayList<UserTestResultCsvRow> rows = new ArrayList<>();

        rows.add(new UserTestResultCsvRow(
            "user_id",
            "user_username",
            "user_anonymous",
            "user_name",
            "user_email",
            "user_age",
            "config_version",
            "attempt_id",
            "group",
            "timestamp",
            "time_taken",
            "correct_questions",
            "questions_answered",
            "accuracy",
            "question_id",
            "question_start",
            "question_end",
            "question_correct",
            "question_skipped",
            "total_option_changes",
            "total_option_hovers",
            "event_option_id",
            "event_type",
            "event_timestamp"
        ));

        final User user = userTestResults.user();

        for (final TestResultsByConfig resultsByConfig : userTestResults.results()) {
            for (final TestResult testResult : resultsByConfig.attemptsResults()) {
                for (final QuestionEventLog questionEvent : testResult.questionEvents()) {
                    final List<QuestionTimestamp> timestamps = questionEvent.timestamps().stream()
                        .sorted(Comparator.comparingLong(QuestionTimestamp::start))
                        .toList();

                    final List<OptionEventLog> optionEventLogs = questionEvent.events().stream()
                        .sorted(Comparator.comparingLong(OptionEventLog::timestamp))
                        .toList();

                    int optionEventIndex = 0;

                    for (final QuestionTimestamp questionTimestamp : timestamps) {
                        if (optionEventLogs.isEmpty()) {
                            rows.add(new UserTestResultCsvRow(
                                Integer.toString(user.id()),
                                user.username(),
                                Boolean.toString(user.anonymous()),
                                user.name(),
                                user.email(),
                                user.age() != null ? Integer.toString(user.age()) : null,
                                Integer.toString(resultsByConfig.configVersion()),
                                Long.toString(testResult.attemptId()),
                                testResult.group(),
                                Long.toString(testResult.timestamp()),
                                Long.toString(testResult.timeTaken()),
                                Long.toString(testResult.correctQuestions()),
                                Long.toString(testResult.questionsAnswered()),
                                Double.toString(testResult.accuracy()),
                                Integer.toString(questionEvent.questionId()),
                                Long.toString(questionTimestamp.start()),
                                Long.toString(questionTimestamp.end()),
                                Boolean.toString(questionEvent.correct()),
                                Boolean.toString(questionEvent.skipped()),
                                Integer.toString(questionEvent.totalOptionChanges()),
                                Integer.toString(questionEvent.totalOptionHovers()),
                                null,
                                null,
                                null
                            ));
                            continue;
                        }

                        for (int i = optionEventIndex; i < optionEventLogs.size(); i++) {
                            final OptionEventLog optionEventLog = optionEventLogs.get(i);

                            if (optionEventLog.timestamp() > questionTimestamp.end()) {
                                optionEventIndex = i;
                                break;
                            }

                            rows.add(new UserTestResultCsvRow(
                                Integer.toString(user.id()),
                                user.username(),
                                Boolean.toString(user.anonymous()),
                                user.name(),
                                user.email(),
                                user.age() != null ? Integer.toString(user.age()) : null,
                                Integer.toString(resultsByConfig.configVersion()),
                                Long.toString(testResult.attemptId()),
                                testResult.group(),
                                Long.toString(testResult.timestamp()),
                                Long.toString(testResult.timeTaken()),
                                Long.toString(testResult.correctQuestions()),
                                Long.toString(testResult.questionsAnswered()),
                                Double.toString(testResult.accuracy()),
                                Integer.toString(questionEvent.questionId()),
                                Long.toString(questionTimestamp.start()),
                                Long.toString(questionTimestamp.end()),
                                Boolean.toString(questionEvent.correct()),
                                Boolean.toString(questionEvent.skipped()),
                                Integer.toString(questionEvent.totalOptionChanges()),
                                Integer.toString(questionEvent.totalOptionHovers()),
                                Integer.toString(optionEventLog.optionId()),
                                optionEventLog.type().getName(),
                                Long.toString(optionEventLog.timestamp())
                            ));
                        }
                    }
                }
            }
        }

        return rows.stream().map(r -> r.toString(CSV_DELIMITER)).collect(Collectors.joining("\n"));
    }
}
