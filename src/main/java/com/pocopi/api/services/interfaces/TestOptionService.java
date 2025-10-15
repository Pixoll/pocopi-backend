package com.pocopi.api.services.interfaces;

import com.pocopi.api.dto.TestGroup.PatchOption;
import com.pocopi.api.models.test.TestOptionModel;
import com.pocopi.api.models.test.TestQuestionModel;
import com.pocopi.api.services.implementations.TestGroupServiceImp;

import java.io.File;
import java.util.List;
import java.util.Map;

public interface TestOptionService {

    Map<String, String> processOptions(
        TestQuestionModel question,
        List<PatchOption> options,
        Map<Integer, File> images,
        TestGroupServiceImp.ImageIndexTracker imageIndexTracker
    );
    List<TestOptionModel> findAllByQuestion(TestQuestionModel question);
    void deleteById(int id);
    void save(TestOptionModel testOptionModel);
    void deleteWithImage(TestOptionModel option);
}
