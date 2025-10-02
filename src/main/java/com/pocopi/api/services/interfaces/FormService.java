package com.pocopi.api.services.interfaces;

import com.pocopi.api.dto.Form.FormAnswerRequest;

public interface FormService {
    void saveUserFormAnswers(FormAnswerRequest request);
}