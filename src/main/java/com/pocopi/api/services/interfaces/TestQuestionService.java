package com.pocopi.api.services.interfaces;

import com.pocopi.api.dto.TestGroup.PatchQuestion;
import com.pocopi.api.models.test.TestPhaseModel;
import com.pocopi.api.models.test.TestQuestionModel;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface TestQuestionService {

    Map<String, String> processTestQuestions(
        TestPhaseModel phase,
        List<PatchQuestion> questions,
        List<Optional<File>> images,
        int imageIndex
    );
    List<TestQuestionModel>  findAllByPhase(TestPhaseModel phase);

    void deleteWithOptions(TestQuestionModel question);
}
