package com.pocopi.api.services;

import com.pocopi.api.dto.form.FormAnswer;
import com.pocopi.api.dto.form.NewFormAnswer;
import com.pocopi.api.dto.form.NewFormAnswers;
import com.pocopi.api.dto.results.FormAnswersByConfig;
import com.pocopi.api.exception.HttpException;
import com.pocopi.api.models.form.*;
import com.pocopi.api.models.test.UserTestAttemptModel;
import com.pocopi.api.repositories.*;
import com.pocopi.api.repositories.projections.UserFormAnswerProjection;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

@Service
public class FormAnswerService {
    private final ConfigRepository configRepository;
    private final FormRepository formRepository;
    private final FormQuestionRepository formQuestionRepository;
    private final FormQuestionOptionRepository formQuestionOptionRepository;
    private final UserFormSubmissionRepository userFormSubmissionRepository;
    private final UserFormAnswerRepository userFormAnswerRepository;
    private final UserTestAttemptRepository userTestAttemptRepository;

    public FormAnswerService(
        ConfigRepository configRepository,
        FormRepository formRepository,
        FormQuestionRepository formQuestionRepository,
        FormQuestionOptionRepository formQuestionOptionRepository,
        UserFormSubmissionRepository userFormSubmissionRepository,
        UserFormAnswerRepository userFormAnswerRepository,
        UserTestAttemptRepository userTestAttemptRepository
    ) {
        this.configRepository = configRepository;
        this.formRepository = formRepository;
        this.formQuestionRepository = formQuestionRepository;
        this.formQuestionOptionRepository = formQuestionOptionRepository;
        this.userFormSubmissionRepository = userFormSubmissionRepository;
        this.userFormAnswerRepository = userFormAnswerRepository;
        this.userTestAttemptRepository = userTestAttemptRepository;
    }

    @Transactional
    public List<FormAnswersByConfig> getUserFormAnswers(int userId) {
        final List<UserFormAnswerProjection> userFormAnswers = userFormAnswerRepository.findAllByUserId(userId);

        final HashMap<Integer, FormAnswersByConfig> formAnswersByConfig = new HashMap<>();

        for (final UserFormAnswerProjection userFormAnswer : userFormAnswers) {
            formAnswersByConfig.putIfAbsent(
                userFormAnswer.getConfigVersion(),
                new FormAnswersByConfig(userFormAnswer.getConfigVersion(), new ArrayList<>(), new ArrayList<>())
            );

            final FormAnswersByConfig answersByFormType = formAnswersByConfig
                .get(userFormAnswer.getConfigVersion());

            final List<FormAnswer> formAnswers = userFormAnswer.getFormType().equals(FormType.PRE.getName())
                ? answersByFormType.preTestForm()
                : answersByFormType.postTestForm();

            final FormAnswer formAnswer = new FormAnswer(
                userFormAnswer.getQuestionId(),
                userFormAnswer.getOptionId() != null ? userFormAnswer.getOptionId() : null,
                userFormAnswer.getValue(),
                userFormAnswer.getAnswer()
            );

            formAnswers.add(formAnswer);
        }

        return formAnswersByConfig.values().stream().toList();
    }

    @Transactional
    public void saveUserFormAnswers(int userId, FormType formType, NewFormAnswers formAnswers) {
        final int configVersion = configRepository.getLastConfig().getVersion();

        final UserTestAttemptModel unfinishedAttempt = userTestAttemptRepository
            .findUnfinishedAttempt(configVersion, userId)
            .orElseThrow(() -> HttpException.notFound("User has not started an attempt yet"));

        final FormModel form = formRepository.findByTypeAndConfigVersion(formType, configVersion)
            .orElseThrow(() -> HttpException.notFound("Form of type " + formType + " not found"));

        if (userFormSubmissionRepository.hasAnsweredForm(unfinishedAttempt.getId(), form.getId())) {
            throw HttpException.conflict("User has already answered form of type " + formType);
        }

        final UserFormSubmissionModel formSubmission = UserFormSubmissionModel.builder()
            .attempt(unfinishedAttempt)
            .form(form)
            .timestamp(Instant.now())
            .build();

        final UserFormSubmissionModel savedSubmission = userFormSubmissionRepository.save(formSubmission);

        final ArrayList<UserFormAnswerModel> answers = new ArrayList<>();
        final HashMap<Integer, HashSet<Integer>> questionAnswers = new HashMap<>();
        final HashMap<Integer, Boolean> questionHasOtherAnswer = new HashMap<>();

        for (final NewFormAnswer answer : formAnswers.answers()) {
            final FormQuestionModel question = formQuestionRepository
                .findByIdAndFormId(answer.questionId(), form.getId())
                .orElseThrow(() -> HttpException.notFound(
                    "Form question with id " + answer.questionId() + " in form " + form.getId() + " not found"
                ));

            if (questionAnswers.containsKey(question.getId())
                && question.getType() != FormQuestionType.SELECT_MULTIPLE
            ) {
                throw HttpException.conflict(
                    "Form question with id "
                        + question.getId()
                        + " cannot have multiple answers (is not of type "
                        + FormQuestionType.SELECT_MULTIPLE.getValue()
                        + ")"
                );
            }

            if (questionAnswers.getOrDefault(question.getId(), new HashSet<>()).contains(answer.optionId())) {
                throw HttpException.conflict(
                    "Form option with id " + answer.optionId() + " is repeated in question " + question.getId()
                );
            }

            if (questionHasOtherAnswer.getOrDefault(question.getId(), false) && answer.answer() != null) {
                throw HttpException.conflict("Form answer is repeated in question " + question.getId());
            }

            if (question.getType() == FormQuestionType.SELECT_MULTIPLE) {
                final int answersAmount = questionAnswers.getOrDefault(question.getId(), new HashSet<>()).size()
                    + (questionHasOtherAnswer.getOrDefault(question.getId(), false) ? 1 : 0)
                    + 1;

                if (answersAmount < question.getMin() || answersAmount > question.getMax()) {
                    throw HttpException.badRequest(
                        "Form question with id " + question.getId() + " has either too few or too many answers"
                    );
                }
            }

            validateFormAnswer(answer, question);

            final FormQuestionOptionModel option = answer.optionId() != null
                ? formQuestionOptionRepository.findByIdAndFormQuestionId(answer.optionId(), question.getId())
                .orElseThrow(() -> HttpException.notFound(
                    "Form option with id " + answer.optionId() + " in question " + question.getId() + " not found"
                ))
                : null;

            final UserFormAnswerModel userAnswer = UserFormAnswerModel.builder()
                .formSubmission(savedSubmission)
                .question(question)
                .option(option)
                .value(answer.value())
                .answer(answer.answer())
                .build();

            answers.add(userAnswer);

            questionHasOtherAnswer.compute(
                question.getId(),
                (questionId, value) -> Boolean.TRUE.equals(value) || answer.answer() != null
            );

            questionAnswers.compute(
                question.getId(),
                (questionId, optionIds) -> {
                    final HashSet<Integer> set = optionIds != null ? optionIds : new HashSet<>();
                    if (option != null) {
                        set.add(option.getId());
                    }
                    return set;
                }
            );
        }

        userFormAnswerRepository.saveAll(answers);
    }

    private static void validateFormAnswer(NewFormAnswer answer, FormQuestionModel question) {
        switch (question.getType()) {
            case SELECT_MULTIPLE,
                 SELECT_ONE -> {
                if (question.getOther()) {
                    if ((answer.optionId() == null) == (answer.answer() == null)) {
                        throw HttpException.conflict(
                            "Form answer for question with id "
                                + question.getId()
                                + " requires either optionId or answer fields, but not both at the same time"
                        );
                    }
                } else {
                    if (answer.optionId() == null) {
                        throw HttpException.badRequest(
                            "Form answer for question with id " + question.getId() + " requires optionId field"
                        );
                    }

                    if (answer.answer() != null) {
                        throw HttpException.badRequest(
                            "Form answer for question with id " + question.getId() + " cannot have answer field"
                        );
                    }
                }

                if (answer.value() != null) {
                    throw HttpException.badRequest(
                        "Form answer for question with id " + question.getId() + " cannot have value field"
                    );
                }
            }

            case SLIDER -> {
                if (answer.value() == null) {
                    throw HttpException.badRequest(
                        "Form answer for question with id " + question.getId() + " requires value field"
                    );
                } else if (answer.value() < question.getMin() || answer.value() > question.getMax()) {
                    throw HttpException.badRequest(
                        "Form answer value for question with id " + question.getId() + " out of bounds"
                    );
                }

                if (answer.optionId() != null || answer.answer() != null) {
                    throw HttpException.badRequest(
                        "Form answer for question with id "
                            + question.getId()
                            + " cannot have optionId or answer fields"
                    );
                }
            }

            case TEXT_SHORT,
                 TEXT_LONG -> {
                if (answer.answer() == null) {
                    throw HttpException.badRequest(
                        "Form answer for question with id " + question.getId() + " requires answer field"
                    );
                }

                if (answer.optionId() != null || answer.value() != null) {
                    throw HttpException.badRequest(
                        "Form answer for question with id "
                            + question.getId()
                            + " cannot have optionId or answer fields"
                    );
                }
            }
        }
    }
}
