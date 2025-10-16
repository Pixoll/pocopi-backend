package com.pocopi.api.mappers;

import com.pocopi.api.dto.FormResult.FormAnswers;
import com.pocopi.api.dto.Results.UserAllResultsResponse;
import com.pocopi.api.dto.Results.UserBasicInfoResponse;
import com.pocopi.api.dto.TestResult.TestQuestionResult;
import com.pocopi.api.models.user.UserModel;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserAllResultsMapper {

    public UserAllResultsResponse toUserAllResultsResponse(
            UserModel user,
            List<FormAnswers> pre,
            List<FormAnswers> post,
            List<TestQuestionResult> questions
    ) {
        UserBasicInfoResponse userInfo = new UserBasicInfoResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getAge() == 0 ? null : (int) user.getAge(),
                user.getGroup() != null ? user.getGroup().getId() : -1
        );

        return new UserAllResultsResponse(
                userInfo,
                new UserAllResultsResponse.UserFormsPart(pre, post),
                new UserAllResultsResponse.UserTestsPart(questions)
        );
    }
}