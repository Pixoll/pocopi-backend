package com.pocopi.api.unit.user;

import com.pocopi.api.models.user.Role;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RoleTest {

    @Test
    void testFromValueUser() {
        Role role = Role.fromValue("USER");
        assertEquals(Role.USER, role);
        assertEquals("USER", role.getValue());
    }

    @Test
    void testFromValueAdmin() {
        Role role = Role.fromValue("ADMIN");
        assertEquals(Role.ADMIN, role);
        assertEquals("ADMIN", role.getValue());
    }

    @Test
    void testFromValueThrowsExceptionOnInvalid() {
        assertThrows(IllegalArgumentException.class, () -> Role.fromValue("INVALID_ROLE"));
    }

    @Test
    void testJsonValue() {
        assertEquals("USER", Role.USER.getValue());
        assertEquals("ADMIN", Role.ADMIN.getValue());
    }
}
