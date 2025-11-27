package com.pocopi.api.unit.test;

import com.pocopi.api.models.test.UserTestOptionLogModel;
import com.pocopi.api.models.test.UserTestAttemptModel;
import com.pocopi.api.models.test.TestOptionModel;
import com.pocopi.api.models.test.TestQuestionModel;
import com.pocopi.api.models.test.TestPhaseModel;
import com.pocopi.api.models.test.TestGroupModel;
import com.pocopi.api.models.config.ConfigModel;
import com.pocopi.api.models.config.ImageModel;
import com.pocopi.api.models.test.TestOptionEventType;
import com.pocopi.api.models.user.UserModel;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class UserTestOptionLogModelTest {

    @Test
    void testBuilderAndFields() {
        UserModel user = UserModel.builder()
                .username("tester")
                .password("123456789012345678901234567890123456789012345678901234567890")
                .anonymous(false)
                .build();

        ConfigModel config = ConfigModel.builder()
                .title("LogConfig")
                .description("Desc")
                .informedConsent("Consent")
                .build();

        TestGroupModel group = TestGroupModel.builder()
                .config(config)
                .label("LogGroup")
                .probability((byte)70)
                .build();

        UserTestAttemptModel attempt = UserTestAttemptModel.builder()
                .user(user)
                .group(group)
                .start(Instant.now())
                .build();

        TestQuestionModel question = TestQuestionModel.builder()
                .phase(TestPhaseModel.builder()
                        .group(group)
                        .order((byte)1)
                        .build())
                .order((byte)2)
                .text("Choose one")
                .build();

        ImageModel image = ImageModel.builder()
                .path("/img/log.png")
                .alt("Log image")
                .build();

        TestOptionModel option = TestOptionModel.builder()
                .question(question)
                .order((byte)1)
                .text("OptionText")
                .image(image)
                .correct(false)
                .build();

        TestOptionEventType type = TestOptionEventType.SELECT;
        Instant timestamp = Instant.parse("2025-11-18T02:00:00Z");

        UserTestOptionLogModel log = UserTestOptionLogModel.builder()
                .attempt(attempt)
                .option(option)
                .type(type)
                .timestamp(timestamp)
                .build();

        assertEquals(attempt, log.getAttempt());
        assertEquals(option, log.getOption());
        assertEquals(type, log.getType());
        assertEquals(timestamp, log.getTimestamp());
        assertEquals(0L, log.getId());
    }

    @Test
    void testSettersAndGetters() {
        UserTestOptionLogModel log = new UserTestOptionLogModel();

        UserTestAttemptModel attempt = UserTestAttemptModel.builder()
                .user(UserModel.builder()
                        .username("setter")
                        .password("123456789012345678901234567890123456789012345678901234567890")
                        .anonymous(false)
                        .build())
                .group(TestGroupModel.builder()
                        .config(ConfigModel.builder()
                                .title("Setter Config")
                                .description("X")
                                .informedConsent("Y")
                                .build())
                        .label("Setter")
                        .probability((byte)32)
                        .build())
                .start(Instant.parse("2025-11-18T02:09:00Z"))
                .build();

        TestOptionModel option = TestOptionModel.builder()
                .question(TestQuestionModel.builder()
                        .phase(TestPhaseModel.builder()
                                .group(TestGroupModel.builder()
                                        .config(ConfigModel.builder()
                                                .title("Q")
                                                .description("A")
                                                .informedConsent("X")
                                                .build())
                                        .label("B")
                                        .probability((byte)8)
                                        .build())
                                .order((byte)2)
                                .build())
                        .order((byte)9)
                        .text("Setter Q?")
                        .build())
                .order((byte)2)
                .text("Setter Option")
                .build();

        TestOptionEventType type = TestOptionEventType.HOVER;
        Instant timestamp = Instant.parse("2025-11-18T02:13:00Z");

        log.setAttempt(attempt);
        log.setOption(option);
        log.setType(type);
        log.setTimestamp(timestamp);

        assertEquals(attempt, log.getAttempt());
        assertEquals(option, log.getOption());
        assertEquals(type, log.getType());
        assertEquals(timestamp, log.getTimestamp());
    }
}
