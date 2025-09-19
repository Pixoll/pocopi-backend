package com.pocopi.api.services.implementations;

import com.pocopi.api.models.TestGroupModel;
import com.pocopi.api.repositories.TestGroupRepository;
import com.pocopi.api.services.interfaces.TestGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TestGroupServiceImp implements TestGroupService {

    TestGroupRepository testGroupRepository;

    @Autowired
    public TestGroupServiceImp(TestGroupRepository testGroupRepository) {
        this.testGroupRepository = testGroupRepository;
    }
    
    @Override
    public TestGroupModel getTestGroup(int id) {
        return testGroupRepository.findById(id).orElse(null);
    }
}
