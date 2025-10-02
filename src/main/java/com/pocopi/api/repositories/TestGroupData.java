package com.pocopi.api.repositories;

public interface TestGroupData {
    int getGroupId();
    int getConfigVersion();
    String getGroupLabel();
    int getProbability();
    String getGreeting();
    int getPhaseOrder();
    int getQuestionOrder();
    int getQuestionImageId();
    int getOptionOrder();
    String getOptionText();
    Integer getOptionImageId();
    boolean getCorrect();
}
