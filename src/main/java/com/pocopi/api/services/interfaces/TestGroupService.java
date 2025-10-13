package com.pocopi.api.services.interfaces;

import com.pocopi.api.dto.TestGroup.Group;
import com.pocopi.api.models.TestGroupModel;

import java.util.List;
import java.util.Map;

public interface TestGroupService {
    TestGroupModel getTestGroup(int id);
    Map<String, Group> buildGroupResponses(int configVersion);
    List<TestGroupModel> finAllTestGroups();
    TestGroupModel saveTestGroup(TestGroupModel testGroupModel);
    void deleteTestGroup(TestGroupModel testGroupModel);
}
