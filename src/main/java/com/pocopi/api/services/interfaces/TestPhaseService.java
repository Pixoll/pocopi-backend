package com.pocopi.api.services.interfaces;

import com.pocopi.api.dto.TestGroup.PatchPhase;
import com.pocopi.api.models.test.TestProtocolModel;
import com.pocopi.api.services.implementations.TestGroupServiceImp;

import java.io.File;
import java.util.List;
import java.util.Map;

public interface TestPhaseService {

    Map<String, String> processPhases(
        TestProtocolModel protocol,
        List<PatchPhase> phases,
        Map<Integer, File> images,
        TestGroupServiceImp.ImageIndexTracker imageIndexTracker
    );

}
