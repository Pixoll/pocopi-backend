package com.pocopi.api.services.implementations;

import com.pocopi.api.dto.Form.FormAnswerRequest;
import com.pocopi.api.models.FormQuestionModel;
import com.pocopi.api.models.FormQuestionOptionModel;
import com.pocopi.api.models.UserFormAnswerModel;
import com.pocopi.api.models.UserModel;
import com.pocopi.api.repositories.FormQuestionOptionRepository;
import com.pocopi.api.repositories.FormQuestionRepository;
import com.pocopi.api.repositories.UserFormAnswerRepository;
import com.pocopi.api.repositories.UserRepository;
import com.pocopi.api.services.interfaces.FormService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class FormServiceImp implements FormService {
    private final UserFormAnswerRepository userFormAnswerRepository;
    private final UserRepository userRepository;
    private final FormQuestionRepository formQuestionRepository;
    private final FormQuestionOptionRepository formQuestionOptionRepository;

    @Autowired
    public FormServiceImp(UserFormAnswerRepository userFormAnswerRepository,
                          UserRepository userRepository,
                          FormQuestionRepository formQuestionRepository,
                          FormQuestionOptionRepository formQuestionOptionRepository) {
        this.userFormAnswerRepository = userFormAnswerRepository;
        this.userRepository = userRepository;
        this.formQuestionRepository = formQuestionRepository;
        this.formQuestionOptionRepository = formQuestionOptionRepository;
    }

    @Override
    public void saveUserFormAnswers(FormAnswerRequest request) {
        UserModel user = userRepository.getUserByUserId(request.userId());
        for (FormAnswerRequest.QuestionAnswer answer : request.answers()) {
            FormQuestionModel question = formQuestionRepository.findById(answer.questionId())
                    .orElseThrow(() -> new IllegalArgumentException("Question not found"));
            FormQuestionOptionModel option = null;
            if (answer.optionId() != null) {
                option = formQuestionOptionRepository.findById(answer.optionId())
                        .orElse(null);
            }
            UserFormAnswerModel userAnswer = UserFormAnswerModel.builder()
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