package com.pocopi.api.unit.test;

import com.pocopi.api.models.test.UserTestAttemptModel;
import com.pocopi.api.models.user.UserModel;
import com.pocopi.api.models.test.TestGroupModel;
import com.pocopi.api.models.config.ConfigModel;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class UserTestAttemptModelTest {

    @Test
    void testBuilderAndFields() {
        UserModel user = UserModel.builder()
                .username("john_doe")
                .password("123456789012345678901234567890123456789012345678901234567890")
                .anonymous(false)
                .build();

        ConfigModel config = ConfigModel.builder()
                .title("AttemptConfig")
                .description("Attempt Desc")
                .informedConsent("Consent")
                .build();

        TestGroupModel group = TestGroupModel.builder()
                .config(config)
                .label("Group A")
                .probability((byte)60)
                .build();

        Instant start = Instant.now();
        Instant end = start.plusSeconds(120);

        UserTestAttemptModel attempt = UserTestAttemptModel.builder()
                .user(user)
                .group(group)
                .start(start)
                .end(end)
                .build();

        assertEquals(user, attempt.getUser());
        assertEquals(group, attempt.getGroup());
        assertEquals(start, attempt.getStart());
        assertEquals(end, attempt.getEnd());
        assertEquals(0L, attempt.getId());
    }

    @Test
    void testDefaults() {
        UserModel user = UserModel.builder()
                .username("jane_doe")
                .password("123456789012345678901234567890123456789012345678901234567890")
                .anonymous(false)
                .build();

        TestGroupModel group = TestGroupModel.builder()
                .config(ConfigModel.builder()
                        .title("DefConfig")
                        .description("D")
                        .informedConsent("C")
                        .build())
                .label("DefGroup")
                .probability((byte)20)
                .build();

        Instant start = Instant.parse("2025-01-01T00:00:00Z");

        UserTestAttemptModel attempt = UserTestAttemptModel.builder()
                .user(user)
                .group(group)
                .start(start)
                .build();

        assertNull(attempt.getEnd());
    }

    @Test
    void testSettersAndGetters() {
        UserTestAttemptModel attempt = new UserTestAttemptModel();
        UserModel user = UserModel.builder()
                .username("setter_user")
                .password("123456789012345678901234567890123456789012345678901234567890")
                .anonymous(false)
                .build();

        TestGroupModel group = TestGroupModel.builder()
                .config(ConfigModel.builder()
                        .title("SetterConfig")
                        .description("X")
                        .informedConsent("Y")
                        .build())
                .label("SetterGroup")
                .probability((byte)40)
                .build();

        Instant start = Instant.parse("2025-11-18T01:00:00Z");
        Instant end = start.plusSeconds(300);

        attempt.setUser(user);
        attempt.setGroup(group);
        attempt.setStart(start);
        attempt.setEnd(end);

        assertEquals(user, attempt.getUser());
        assertEquals(group, attempt.getGroup());
        assertEquals(start, attempt.getStart());
        assertEquals(end, attempt.getEnd());
    }
}
