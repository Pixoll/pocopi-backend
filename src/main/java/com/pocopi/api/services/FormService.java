package com.pocopi.api.services;

import com.pocopi.api.dto.form.NewFormAnswer;
import com.pocopi.api.dto.form.NewFormAnswers;
import com.pocopi.api.models.form.FormQuestionModel;
import com.pocopi.api.models.form.FormQuestionOptionModel;
import com.pocopi.api.models.form.UserFormAnswerModel;
import com.pocopi.api.models.user.UserModel;
import com.pocopi.api.repositories.FormQuestionOptionRepository;
import com.pocopi.api.repositories.FormQuestionRepository;
import com.pocopi.api.repositories.UserFormAnswerRepository;
import com.pocopi.api.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FormService {
    private final UserFormAnswerRepository userFormAnswerRepository;
    private final UserRepository userRepository;
    private final FormQuestionRepository formQuestionRepository;
    private final FormQuestionOptionRepository formQuestionOptionRepository;

    @Autowired
    public FormService(
        UserFormAnswerRepository userFormAnswerRepository,
        UserRepository userRepository,
        FormQuestionRepository formQuestionRepository,
        FormQuestionOptionRepository formQuestionOptionRepository
    ) {
        this.userFormAnswerRepository = userFormAnswerRepository;
        this.userRepository = userRepository;
        this.formQuestionRepository = formQuestionRepository;
        this.formQuestionOptionRepository = formQuestionOptionRepository;
    }

    public void saveUserFormAnswers(NewFormAnswers request) {
        final UserModel user = userRepository.findByUsername(request.username());
        for (final NewFormAnswer answer : request.answers()) {
            final FormQuestionModel question = formQuestionRepository.findById(answer.questionId())
                .orElseThrow(() -> new IllegalArgumentException("Question not found"));
            FormQuestionOptionModel option = null;
            if (answer.optionId() != null) {
                option = formQuestionOptionRepository.findById(answer.optionId())
                    .orElse(null);
            }
            final UserFormAnswerModel userAnswer = UserFormAnswerModel.builder()
                .user(user)
                .question(question)
                .option(option)
                .value(answer.value() != null ? answer.value().shortValue() : null)
                .answer(answer.answer())
                .build();
            userFormAnswerRepository.save(userAnswer);
        }
    }
}
