package com.pocopi.api.services;

import com.pocopi.api.dto.form.NewFormAnswer;
import com.pocopi.api.dto.form.NewFormAnswers;
import com.pocopi.api.models.form.*;
import com.pocopi.api.models.user.UserModel;
import com.pocopi.api.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class FormService {
    private final UserFormAnswerRepository userFormAnswerRepository;
    private final UserRepository userRepository;
    private final FormQuestionRepository formQuestionRepository;
    private final FormQuestionOptionRepository formQuestionOptionRepository;
    private final FormRepository formRepository;
    private final UserFormSubmissionRepository userFormSubmissionRepository;

    @Autowired
    public FormService(
        UserFormAnswerRepository userFormAnswerRepository,
        UserRepository userRepository,
        FormQuestionRepository formQuestionRepository,
        FormQuestionOptionRepository formQuestionOptionRepository,
        FormRepository formRepository,
        UserFormSubmissionRepository userFormSubmissionRepository
    ) {
        this.userFormAnswerRepository = userFormAnswerRepository;
        this.userRepository = userRepository;
        this.formQuestionRepository = formQuestionRepository;
        this.formQuestionOptionRepository = formQuestionOptionRepository;
        this.formRepository = formRepository;
        this.userFormSubmissionRepository = userFormSubmissionRepository;
    }

    public void saveUserFormAnswers(NewFormAnswers request) {
        final UserModel user = userRepository.findByUsername(request.username());
        final FormModel form = formRepository.findById(request.formId());

        final UserFormSubmissionModel formSubmission = UserFormSubmissionModel.builder()
            .user(user)
            .form(form)
            .timestamp(Instant.now())
            .build();

        userFormSubmissionRepository.save(formSubmission);

        for (final NewFormAnswer answer : request.answers()) {
            final FormQuestionModel question = formQuestionRepository.findById(answer.questionId())
                .orElseThrow(() -> new IllegalArgumentException("Question not found"));

            final FormQuestionOptionModel option = answer.optionId() != null
                ? formQuestionOptionRepository.findById(answer.optionId()).orElse(null)
                : null;

            final UserFormAnswerModel userAnswer = UserFormAnswerModel.builder()
                .formSubmission(formSubmission)
                .question(question)
                .option(option)
                .value(answer.value() != null ? answer.value().shortValue() : null)
                .answer(answer.answer())
                .build();
            userFormAnswerRepository.save(userAnswer);
        }
    }
}
