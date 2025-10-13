package com.pocopi.api.services.interfaces;

import com.pocopi.api.dto.TestGroup.PatchQuestion;
import com.pocopi.api.models.test.TestPhaseModel;
import com.pocopi.api.models.test.TestQuestionModel;

import java.io.File;
import java.util.List;
import java.util.Map;

public interface TestQuestionService {

    Map<String, String> processTestQuestions(
        TestPhaseModel phase,
        List<PatchQuestion> questions,
        List<File> images,
        int imageIndex
    );
    List<TestQuestionModel>  findAllByPhase(TestPhaseModel phase);

    void deleteWithOptions(TestQuestionModel question);
}
