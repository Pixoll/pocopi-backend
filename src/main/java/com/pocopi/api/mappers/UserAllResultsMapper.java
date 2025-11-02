package com.pocopi.api.mappers;

import com.pocopi.api.dto.form.FormAnswers;
import com.pocopi.api.dto.results.ResultsByUser;
import com.pocopi.api.dto.results.TestQuestionResult;
import com.pocopi.api.dto.user.UserBasicInfo;
import com.pocopi.api.models.user.UserModel;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserAllResultsMapper {
    public ResultsByUser toUserAllResultsResponse(
        UserModel user,
        List<FormAnswers> pre,
        List<FormAnswers> post,
        List<TestQuestionResult> questions
    ) {
        final UserBasicInfo userInfo = new UserBasicInfo(
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getAge() == 0 ? null : (int) user.getAge()
        );

        return new ResultsByUser(
            userInfo,
            new ResultsByUser.UserFormsResult(pre, post),
            new ResultsByUser.UserTestsResult(questions)
        );
    }
}
