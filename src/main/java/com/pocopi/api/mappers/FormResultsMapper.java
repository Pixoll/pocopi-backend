package com.pocopi.api.mappers;

import com.pocopi.api.dto.FormResult.*;
import com.pocopi.api.dto.Results.UserBasicInfoResponse;
import com.pocopi.api.models.form.*;
import com.pocopi.api.models.user.UserModel;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class FormResultsMapper {

    public UserBasicInfoResponse toUserBasicInfo(UserModel user) {
        return new UserBasicInfoResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getAge() == 0 ? null : (int) user.getAge()
        );
    }

    public UserFormWithInfoResultsResponse toUserFormResultsResponse(UserModel user, List<UserFormAnswerModel> userAnswers) {
        Map<FormType, Map<Integer, List<UserFormAnswerModel>>> answersByTypeAndForm = userAnswers.stream()
                .collect(Collectors.groupingBy(
                        a -> a.getQuestion().getForm().getType(),
                        Collectors.groupingBy(a -> a.getQuestion().getForm().getId())
                ));

        List<FormAnswers> pre = buildFormAnswers(answersByTypeAndForm.get(FormType.PRE));
        List<FormAnswers> post = buildFormAnswers(answersByTypeAndForm.get(FormType.POST));

        return new UserFormWithInfoResultsResponse(toUserBasicInfo(user), pre, post);
    }

    public GroupFormResultsResponse toGroupFormResultsResponse(int groupId, List<UserFormWithInfoResultsResponse> users) {
        return new GroupFormResultsResponse(groupId, users);
    }

    private List<FormAnswers> buildFormAnswers(Map<Integer, List<UserFormAnswerModel>> byForm) {
        if (byForm == null) return Collections.emptyList();
        return byForm.entrySet().stream().map(entry -> {
            int formId = entry.getKey();
            List<UserFormAnswerModel> answers = entry.getValue();
            String formTitle = answers.get(0).getQuestion().getForm().getTitle();
            List<QuestionAnswer> questionAnswers = answers.stream().map(a -> {
                Integer optionId = a.getOption() != null ? a.getOption().getId() : null;
                String optionText = a.getOption() != null ? a.getOption().getText() : null;
                return new QuestionAnswer(
                        a.getQuestion().getId(),
                        a.getQuestion().getText(),
                        optionId,
                        optionText,
                        a.getValue() != null ? a.getValue().intValue() : null,
                        a.getAnswer()
                );
            }).toList();
            return new FormAnswers(formId, formTitle, questionAnswers);
        }).toList();
    }
}