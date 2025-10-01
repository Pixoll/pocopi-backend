package com.pocopi.api.dto.Form;

import com.pocopi.api.dto.FormQuestion.FormQuestion;

import java.util.List;

public record Form(
 List<FormQuestion> questions
) {
}
