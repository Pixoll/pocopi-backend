package com.pocopi.api.repositories;

public interface TestGroupData {
    int getGroupId();
    int getConfigVersion();
    String getGroupLabel();
    int getProbability();
    String getGreeting();
    int getProtocolId();
    String getProtocolLabel();
    boolean getAllowPreviousPhase();
    boolean getAllowPreviousQuestion();
    boolean getAllowSkipQuestion();
    int getPhaseId();
    int getQuestionId();
    String getQuestionText();
    int getQuestionOrder();
    int getQuestionImageId();
    int getOptionId();
    String getOptionText();
    Integer getOptionImageId();
    boolean getCorrect();
}
