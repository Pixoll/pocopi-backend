package com.pocopi.api.services.interfaces;

import com.pocopi.api.dto.TestGroup.PatchOption;
import com.pocopi.api.models.TestOptionModel;
import com.pocopi.api.models.TestQuestionModel;

import java.io.File;
import java.util.List;
import java.util.Map;

public interface TestOptionService {

    Map<String, String> processOptions(
        TestQuestionModel question,
        List<PatchOption> options,
        List<File> images,
        int imageIndex
    );
    List<TestOptionModel> findAllByQuestion(TestQuestionModel question);
    void deleteById(int id);
    void save(TestOptionModel testOptionModel);
    void deleteWithImage(TestOptionModel option);
}
