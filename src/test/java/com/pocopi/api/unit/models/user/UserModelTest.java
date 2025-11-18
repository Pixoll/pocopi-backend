package com.pocopi.api.unit.models.user;

import com.pocopi.api.models.user.Role;
import com.pocopi.api.models.user.UserModel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserModelTest {

    @Test
    void testUserModelBuilderAndGetters() {
        String username = "JohnDee";
        String name = "John Dee";
        String email = "john.dee@example.com";
        Byte age = 25;
        String encryptedPassword = "123456789012345678901234567890123456789012345678901234567890";
        Role role = Role.ADMIN;

        UserModel user = UserModel.builder()
                .username(username)
                .name(name)
                .email(email)
                .age(age)
                .anonymous(false)
                .role(role)
                .password(encryptedPassword)
                .build();

        assertEquals(username, user.getUsername());
        assertEquals(name, user.getName());
        assertEquals(email, user.getEmail());
        assertEquals(age, user.getAge());
        assertEquals(role, user.getRole());
        assertEquals(encryptedPassword, user.getPassword());
        assertFalse(user.isAnonymous());
    }

    @Test
    void testDefaultRoleIsUser() {
        UserModel user = UserModel.builder()
                .username("test")
                .password("123456789012345678901234567890123456789012345678901234567890")
                .anonymous(false)
                .build();

        assertEquals(Role.USER, user.getRole());
    }
}
