package com.pocopi.api.services.interfaces;

import com.pocopi.api.dto.TestGroup.PatchOption;
import com.pocopi.api.models.test.TestOptionModel;
import com.pocopi.api.models.test.TestQuestionModel;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface TestOptionService {

    Map<String, String> processOptions(
        TestQuestionModel question,
        List<PatchOption> options,
        List<Optional<File>> images,
        int imageIndex
    );
    List<TestOptionModel> findAllByQuestion(TestQuestionModel question);
    void deleteById(int id);
    void save(TestOptionModel testOptionModel);
    void deleteWithImage(TestOptionModel option);
}
