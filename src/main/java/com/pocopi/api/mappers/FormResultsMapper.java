package com.pocopi.api.mappers;

import com.pocopi.api.dto.form.FormAnswer;
import com.pocopi.api.dto.form.FormAnswers;
import com.pocopi.api.dto.results.FormAnswersByGroup;
import com.pocopi.api.dto.results.FormAnswersByUser;
import com.pocopi.api.dto.user.UserBasicInfo;
import com.pocopi.api.models.form.FormType;
import com.pocopi.api.models.form.UserFormAnswerModel;
import com.pocopi.api.models.user.UserModel;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class FormResultsMapper {
    public UserBasicInfo toUserBasicInfo(UserModel user) {
        return new UserBasicInfo(
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getAge() == 0 ? null : (int) user.getAge()
        );
    }

    public FormAnswersByUser toUserFormResultsResponse(UserModel user, List<UserFormAnswerModel> userAnswers) {
        final Map<FormType, Map<Integer, List<UserFormAnswerModel>>> answersByTypeAndForm = userAnswers.stream()
            .collect(Collectors.groupingBy(
                a -> a.getQuestion().getForm().getType(),
                Collectors.groupingBy(a -> a.getQuestion().getForm().getId())
            ));

        final List<FormAnswers> pre = buildFormAnswers(answersByTypeAndForm.get(FormType.PRE));
        final List<FormAnswers> post = buildFormAnswers(answersByTypeAndForm.get(FormType.POST));

        return new FormAnswersByUser(toUserBasicInfo(user), pre, post);
    }

    public FormAnswersByGroup toGroupFormResultsResponse(int groupId, List<FormAnswersByUser> users) {
        return new FormAnswersByGroup(groupId, users);
    }

    private List<FormAnswers> buildFormAnswers(Map<Integer, List<UserFormAnswerModel>> byForm) {
        if (byForm == null) {
            return Collections.emptyList();
        }

        return byForm.entrySet().stream().map(entry -> {
            final int formId = entry.getKey();
            final List<UserFormAnswerModel> answers = entry.getValue();
            final String formTitle = answers.getFirst().getQuestion().getForm().getTitle();
            final List<FormAnswer> formAnswers = answers.stream().map(a -> {
                final Integer optionId = a.getOption() != null ? a.getOption().getId() : null;
                final String optionText = a.getOption() != null ? a.getOption().getText() : null;
                return new FormAnswer(
                    a.getQuestion().getId(),
                    a.getQuestion().getText(),
                    optionId,
                    optionText,
                    a.getValue() != null ? a.getValue().intValue() : null,
                    a.getAnswer()
                );
            }).toList();
            return new FormAnswers(formId, formTitle, formAnswers);
        }).toList();
    }
}
