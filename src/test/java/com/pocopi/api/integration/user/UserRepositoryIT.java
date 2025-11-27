package com.pocopi.api.integration.user;

import com.pocopi.api.models.user.Role;
import com.pocopi.api.models.user.UserModel;
import com.pocopi.api.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("integration")
class UserRepositoryIT {

    private static final Logger log = LoggerFactory.getLogger(UserRepositoryIT.class);

    @Autowired
    private UserRepository userRepository;

    @Test
    @Transactional
    void createMultipleUsersAndQueryByFields() {
        log.info("----------- Iniciando UserIT.createMultipleUsersAndQueryByFields -----------");

        // Test de creacion de usuario no anonimo 1
        UserModel user1 = UserModel.builder()
            .username("integration_user_1")
            .role(Role.USER)
            .anonymous(false)
            .name("Integration User 1")
            .email("integration1@example.com")
            .age((byte) 20)
            .password("x".repeat(UserModel.ENCRYPTED_PASSWORD_LEN))
            .build();

        // Test de creacion de usuario no anonimo 2
        UserModel user2 = UserModel.builder()
            .username("integration_user_2")
            .role(Role.ADMIN)
            .anonymous(false)
            .name("Integration User 2")
            .email("integration2@example.com")
            .age((byte) 30)
            .password("x".repeat(UserModel.ENCRYPTED_PASSWORD_LEN))
            .build();

        // Test de creacion de usuario anonimo
        UserModel user3 = UserModel.builder()
            .username("integration_user_3")
            .role(Role.USER)
            .anonymous(true)
            .name(null)
            .email(null)
            .age(null)
            .password("x".repeat(UserModel.ENCRYPTED_PASSWORD_LEN))
            .build();

        UserModel saved1 = userRepository.save(user1);
        UserModel saved2 = userRepository.save(user2);
        UserModel saved3 = userRepository.save(user3);

        log.info("Usuarios guardados: {}, {}, {}",
            saved1.getUsername(), saved2.getUsername(), saved3.getUsername());

        assertTrue(saved1.getId() > 0);
        assertTrue(saved2.getId() > 0);
        assertTrue(saved3.getId() > 0);

        assertTrue(userRepository.existsByUsername("integration_user_1"));
        assertTrue(userRepository.existsByUsername("integration_user_2"));
        assertTrue(userRepository.existsByUsername("integration_user_3"));
        assertFalse(userRepository.existsByUsername("non_existing_user"));

        assertTrue(userRepository.existsByEmail("integration1@example.com"));
        assertTrue(userRepository.existsByEmail("integration2@example.com"));
        assertFalse(userRepository.existsByEmail("integration3@example.com")); // es null, no cuenta
        assertFalse(userRepository.existsByEmail("non_existing@example.com"));

        Optional<UserModel> found2 = userRepository.findByUsername("integration_user_2");
        assertTrue(found2.isPresent());
        assertEquals("integration2@example.com", found2.get().getEmail());

        UserModel foundById = userRepository.getUserByUserId(saved1.getId());
        assertEquals("integration_user_1", foundById.getUsername());
        assertEquals("integration1@example.com", foundById.getEmail());

        List<UserModel> usersRoleUser = userRepository.findAllByRole(Role.USER);
        List<UserModel> usersRoleAdmin = userRepository.findAllByRole(Role.ADMIN);

        assertTrue(usersRoleUser.stream().anyMatch(u -> u.getUsername().equals("integration_user_1")));
        assertTrue(usersRoleUser.stream().anyMatch(u -> u.getUsername().equals("integration_user_3")));
        assertTrue(usersRoleAdmin.stream().anyMatch(u -> u.getUsername().equals("integration_user_2")));

        log.info("----------- Finalizó correctamente UserIT.createMultipleUsersAndQueryByFields -----------");
    }
}
