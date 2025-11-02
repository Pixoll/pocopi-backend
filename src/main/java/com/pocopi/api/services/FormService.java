package com.pocopi.api.services;

import com.pocopi.api.dto.form.NewFormAnswer;
import com.pocopi.api.dto.form.NewFormAnswers;
import com.pocopi.api.exception.HttpException;
import com.pocopi.api.models.form.*;
import com.pocopi.api.models.user.UserModel;
import com.pocopi.api.repositories.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

@Service
public class FormService {
    private final FormRepository formRepository;
    private final FormQuestionRepository formQuestionRepository;
    private final FormQuestionOptionRepository formQuestionOptionRepository;
    private final UserFormSubmissionRepository userFormSubmissionRepository;
    private final UserFormAnswerRepository userFormAnswerRepository;

    public FormService(
        FormRepository formRepository,
        FormQuestionRepository formQuestionRepository,
        FormQuestionOptionRepository formQuestionOptionRepository,
        UserFormSubmissionRepository userFormSubmissionRepository,
        UserFormAnswerRepository userFormAnswerRepository
    ) {
        this.formRepository = formRepository;
        this.formQuestionRepository = formQuestionRepository;
        this.formQuestionOptionRepository = formQuestionOptionRepository;
        this.userFormSubmissionRepository = userFormSubmissionRepository;
        this.userFormAnswerRepository = userFormAnswerRepository;
    }

    @Transactional
    public void saveUserFormAnswers(UserModel user, NewFormAnswers formAnswers) {
        final FormModel form = formRepository.findById(formAnswers.formId())
            .orElseThrow(() -> HttpException.notFound("Form with id " + formAnswers.formId() + " not found"));

        final UserFormSubmissionModel formSubmission = UserFormSubmissionModel.builder()
            .user(user)
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
                .value(answer.value() != null ? answer.value().shortValue() : null)
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
