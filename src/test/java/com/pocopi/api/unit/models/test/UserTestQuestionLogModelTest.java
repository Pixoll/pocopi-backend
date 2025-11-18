package com.pocopi.api.unit.models.test;

import com.pocopi.api.models.test.UserTestQuestionLogModel;
import com.pocopi.api.models.test.UserTestAttemptModel;
import com.pocopi.api.models.test.TestQuestionModel;
import com.pocopi.api.models.test.TestPhaseModel;
import com.pocopi.api.models.test.TestGroupModel;
import com.pocopi.api.models.config.ConfigModel;
import com.pocopi.api.models.user.UserModel;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class UserTestQuestionLogModelTest {

    @Test
    void testBuilderAndFields() {
        UserModel user = UserModel.builder()
                .username("user1")
                .password("123456789012345678901234567890123456789012345678901234567890")
                .anonymous(false)
                .build();

        ConfigModel config = ConfigModel.builder()
                .title("QLogConfig")
                .description("Desc")
                .informedConsent("Consent")
                .build();

        TestGroupModel group = TestGroupModel.builder()
                .config(config)
                .label("QGroup")
                .probability((byte)80)
                .build();

        UserTestAttemptModel attempt = UserTestAttemptModel.builder()
                .user(user)
                .group(group)
                .start(Instant.parse("2025-11-18T02:00:00Z"))
                .build();

        TestQuestionModel question = TestQuestionModel.builder()
                .phase(TestPhaseModel.builder()
                        .group(group)
                        .order((byte)1)
                        .build())
                .order((byte)3)
                .text("Log Question?")
                .build();

        Instant timestamp = Instant.parse("2025-11-18T02:10:00Z");
        int duration = 180;

        UserTestQuestionLogModel log = UserTestQuestionLogModel.builder()
                .attempt(attempt)
                .question(question)
                .timestamp(timestamp)
                .duration(duration)
                .build();

        assertEquals(attempt, log.getAttempt());
        assertEquals(question, log.getQuestion());
        assertEquals(timestamp, log.getTimestamp());
        assertEquals(duration, log.getDuration());
        assertEquals(0L, log.getId());
    }

    @Test
    void testSettersAndGetters() {
        UserTestQuestionLogModel log = new UserTestQuestionLogModel();

        UserTestAttemptModel attempt = UserTestAttemptModel.builder()
                .user(UserModel.builder()
                        .username("setter_user")
                        .password("123456789012345678901234567890123456789012345678901234567890")
                        .anonymous(false)
                        .build())
                .group(TestGroupModel.builder()
                        .config(ConfigModel.builder()
                                .title("SetterQ")
                                .description("X")
                                .informedConsent("Y")
                                .build())
                        .label("SetterGroup")
                        .probability((byte)40)
                        .build())
                .start(Instant.parse("2025-11-18T02:29:00Z"))
                .build();

        TestQuestionModel question = TestQuestionModel.builder()
                .phase(TestPhaseModel.builder()
                        .group(TestGroupModel.builder()
                                .config(ConfigModel.builder()
                                        .title("Ph")
                                        .description("Hm")
                                        .informedConsent("Ok")
                                        .build())
                                .label("Gr")
                                .probability((byte)2)
                                .build())
                        .order((byte)2)
                        .build())
                .order((byte)4)
                .text("Setter Question?")
                .build();

        Instant timestamp = Instant.parse("2025-11-18T02:31:15Z");
        int duration = 47;

        log.setAttempt(attempt);
        log.setQuestion(question);
        log.setTimestamp(timestamp);
        log.setDuration(duration);

        assertEquals(attempt, log.getAttempt());
        assertEquals(question, log.getQuestion());
        assertEquals(timestamp, log.getTimestamp());
        assertEquals(duration, log.getDuration());
    }
}
