package com.pocopi.api.services.interfaces;

import com.pocopi.api.dto.TestGroup.PatchPhase;
import com.pocopi.api.models.test.TestProtocolModel;

import java.io.File;
import java.util.List;
import java.util.Map;

public interface TestPhaseService {

    Map<String, String> processPhases(
        TestProtocolModel protocol,
        List<PatchPhase> phases,
        List<File> images,
        int imageIndex
    );

}
