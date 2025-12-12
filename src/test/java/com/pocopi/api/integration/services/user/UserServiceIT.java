package com.pocopi.api.integration.services.user;

import com.pocopi.api.dto.auth.CredentialsUpdate;
import com.pocopi.api.dto.user.NewAdmin;
import com.pocopi.api.dto.user.NewUser;
import com.pocopi.api.exception.HttpException;
import com.pocopi.api.exception.MultiFieldException;
import com.pocopi.api.models.config.ConfigModel;
import com.pocopi.api.models.user.Role;
import com.pocopi.api.models.user.UserModel;
import com.pocopi.api.repositories.ConfigRepository;
import com.pocopi.api.repositories.UserRepository;
import com.pocopi.api.services.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("integration")
class UserServiceIT {

    @Autowired
    private UserService userService;

    @Autowired
    private ConfigRepository configRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @Transactional
    void createUser_AsAnonymous_WhenConfigAnonymousTrue_ShouldCreateUser() {
        // Arrange
        ConfigModel cfg = ConfigModel.builder()
            .title("cfg-a")
            .description("d")
            .informedConsent("c")
            .anonymous(true)
            .build();
        cfg = configRepository.save(cfg);

        NewUser newUser = new NewUser("anonuser", null, null, null, "password123");

        // Act
        userService.createUser(newUser);

        // Assert
        assertTrue(userRepository.existsByUsername("anonuser"));
        UserModel saved = userRepository.findByUsername("anonuser").orElseThrow();
        assertTrue(saved.isAnonymous());
        assertEquals("anonuser", saved.getUsername());
        assertTrue(passwordEncoder.matches("password123", saved.getPassword()));
    }

    @Test
    @Transactional
    void createUser_AsNonAnonymous_MissingFields_ShouldThrowMultiFieldException() {
        // Arrange
        ConfigModel cfg = ConfigModel.builder()
            .title("cfg-b")
            .description("d")
            .informedConsent("c")
            .anonymous(false)
            .build();
        cfg = configRepository.save(cfg);

        NewUser invalid = new NewUser("u1", null, null, null, "password123");

        // Act & Assert
        assertThrows(MultiFieldException.class, () -> userService.createUser(invalid));
    }

    @Test
    @Transactional
    void createAdmin_Valid_ShouldCreateAdmin() {
        // Arrange
        NewAdmin admin = new NewAdmin("admin1", "adminpass");

        // Act
        userService.createAdmin(admin);

        // Assert
        assertTrue(userRepository.existsByUsername("admin1"));
        UserModel saved = userRepository.findByUsername("admin1").orElseThrow();
        assertEquals(Role.ADMIN, saved.getRole());
        assertTrue(saved.isAnonymous());
        assertTrue(passwordEncoder.matches("adminpass", saved.getPassword()));
    }

    @Test
    @Transactional
    void updateCredentials_SuccessfullyChangesUsernameAndPassword() {
        // Arrange
        String oldUsername = "u_to_update";
        String oldPass = "oldPassword1";
        UserModel user = UserModel.builder()
            .username(oldUsername)
            .role(Role.USER)
            .anonymous(true)
            .password(passwordEncoder.encode(oldPass))
            .build();
        user = userRepository.save(user);

        CredentialsUpdate creds = new CredentialsUpdate(oldUsername, "newUser", oldPass, "newPass123", "newPass123");

        // Act
        userService.updateCredentials(user, creds);

        // Assert
        UserModel reloaded = userRepository.findById(user.getId()).orElseThrow();
        assertEquals("newUser", reloaded.getUsername());
        assertTrue(passwordEncoder.matches("newPass123", reloaded.getPassword()));
    }

    @Test
    @Transactional
    void updateCredentials_WrongOldPassword_ShouldThrowUnauthorized() {
        // Arrange
        String oldUsername = "u_fail";
        String oldPass = "correctOld";
        UserModel user = UserModel.builder()
            .username(oldUsername)
            .role(Role.USER)
            .anonymous(true)
            .password(passwordEncoder.encode(oldPass))
            .build();
        user = userRepository.save(user);

        CredentialsUpdate creds = new CredentialsUpdate(oldUsername, "newUserX", "wrongOld", "pw1", "pw1");

        // Act & Assert
        UserModel finalUser = user;
        HttpException ex = assertThrows(HttpException.class, () -> userService.updateCredentials(finalUser, creds));
        assertEquals(401, ex.getStatus().value());
    }

    @Test
    @Transactional
    void getAllAdminsAndUsers_ShouldReturnExpectedLists() {
        // Arrange
        UserModel u1 = UserModel.builder()
            .username("user1")
            .role(Role.USER)
            .anonymous(false)
            .name("User One")
            .email("u1@example.com")
            .age((byte) 30)
            .password(passwordEncoder.encode("paaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"))
            .build();
        userRepository.save(u1);

        UserModel a1 = UserModel.builder()
            .username("adminX")
            .role(Role.ADMIN)
            .anonymous(true)
            .password(passwordEncoder.encode("paaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"))
            .build();
        userRepository.save(a1);

        // Act
        var users = userService.getAllUsers();
        var admins = userService.getAllAdmins();

        // Assert
        assertTrue(users.stream().anyMatch(u -> "user1".equals(u.username())));
        assertTrue(admins.stream().anyMatch(a -> "adminX".equals(a.username())));
    }

    @Test
    @Transactional
    void getByUsername_NonExisting_ShouldThrowNotFound() {
        // Arrange / Act & Assert
        assertThrows(HttpException.class, () -> userService.getByUsername("no-such-user"));
    }
}