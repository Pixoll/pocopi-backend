package com.pocopi.api.services.implementations;

import com.pocopi.api.dto.User.CreateUserRequest;
import com.pocopi.api.dto.User.User;
import com.pocopi.api.dto.api.FieldErrorResponse;
import com.pocopi.api.exception.MultiFieldException;
import com.pocopi.api.models.test.TestGroupModel;
import com.pocopi.api.models.user.UserModel;
import com.pocopi.api.repositories.UserRepository;
import com.pocopi.api.services.interfaces.TestGroupService;
import com.pocopi.api.services.interfaces.UserService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class UserServiceImp implements UserService {
    private final UserRepository userRepository;
    private final TestGroupService testGroupService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImp(UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          TestGroupService testGroupService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.testGroupService = testGroupService;
    }

    @Override
    public List<User> getAll() {
        List<UserModel> users = userRepository.getAllUsers();
        List<User> usersResponse = new ArrayList<>();
        for (UserModel user : users) {
            usersResponse.add(new User(user.getId(), user.getUsername(), user.getName(), user.isAnonymous(),
                user.getEmail(), user.getAge()));
        }
        return usersResponse;
    }

    @Override
    @Transactional
    public String createUser(CreateUserRequest request) throws MultiFieldException {
        List<FieldErrorResponse> fieldErrors = new ArrayList<>();

        TestGroupModel group = testGroupService.getTestGroup(request.groupId());
        if (group == null) {
            fieldErrors.add(new FieldErrorResponse("groupId", "Test group does not exist"));
        }

        if (request.username().isPresent()) {
            if (userRepository.existsByUsername(request.username().get())) {
                fieldErrors.add(new FieldErrorResponse("username", "Username already exists"));
            }
        }

        if (request.email() != null && !request.email().trim().isEmpty()) {
            if (userRepository.existsByEmail(request.email())) {
                fieldErrors.add(new FieldErrorResponse("email", "Email already exists"));
            }
        }

        validateOnCreateUser(request, fieldErrors);

        if (request.age() < 1 || request.age() > 120) {
            fieldErrors.add(new FieldErrorResponse("age", "Invalid age"));
        }

        validateFieldLengths(request, fieldErrors);

        if (!fieldErrors.isEmpty()) {
            throw new MultiFieldException("Some error in fields", fieldErrors);
        }
        try {
            UserModel newUser = UserModel.builder()
                .group(group)
                .username(request.username().isPresent() ? request.username().get() : generateUniqueUsername())
                .anonymous(request.anonymous())
                .name(request.name())
                .email(request.email())
                .age((byte) request.age())
                .password(passwordEncoder.encode(request.password()))
                .build();

            userRepository.insertNewUser(
                newUser.getUsername(),
                newUser.getGroup().getId(),
                newUser.isAnonymous(),
                newUser.getName(),
                newUser.getEmail(),
                newUser.getAge(),
                newUser.getPassword()
            );

            return "User created successfully";

        } catch (Exception e) {
            throw new RuntimeException("Failed to create user: " + e.getMessage());
        }
    }

    @Override
    public UserModel getUserById(int id) {
        return userRepository.getUserByUserId(id);
    }

    @Override
    public User getByUsername(String username) throws RuntimeException {
        if (!userRepository.existsByUsername(username)) {
            throw new RuntimeException("Username not found");
        }
        UserModel savedUser = userRepository.findByUsername(username);
        return new User(savedUser.getId(),
            savedUser.getUsername(),
            savedUser.getName(),
            savedUser.isAnonymous(),
            savedUser.getEmail(),
            savedUser.getAge()
        );
    }

    @Override
    public List<Integer> getAllUserIds() {
        return userRepository.getAllUserIds();
    }

    private void validateOnCreateUser(CreateUserRequest request, List<FieldErrorResponse> fieldErrors) {
        if (request.anonymous()) {
            if (request.email() != null && !request.email().trim().isEmpty()) {
                fieldErrors.add(new FieldErrorResponse("email", "Email must be null for anonymous users"));
            }

        } else {
            if (request.name() == null || request.name().trim().isEmpty()) {
                fieldErrors.add(new FieldErrorResponse("name", "Name is required for non-anonymous users"));
            }
            if (request.email() == null || request.email().trim().isEmpty()) {
                fieldErrors.add(new FieldErrorResponse("email", "Email is required for non-anonymous users"));
            }
        }
    }

    private void validateFieldLengths(CreateUserRequest request, List<FieldErrorResponse> fieldErrors) {
        if (request.username().isPresent()) {
            String username = request.username().get();
            if (username.length() > 32) {
                fieldErrors.add(new FieldErrorResponse("username", "Username cannot exceed 32 characters"));
            }
            if (username.trim().isEmpty()) {
                fieldErrors.add(new FieldErrorResponse("username", "Username cannot be empty"));
            }
        }

        if (request.name() != null && request.name().length() > 50) {
            fieldErrors.add(new FieldErrorResponse("name", "Name cannot exceed 50 characters"));
        }
        if (request.email() != null && request.email().length() > 50) {
            fieldErrors.add(new FieldErrorResponse("email", "Email cannot exceed 50 characters"));
        }

        if (request.password() != null && request.password().trim().isEmpty()) {
            fieldErrors.add(new FieldErrorResponse("password", "Password cannot be empty"));
        }

        if (request.email() != null && !request.email().trim().isEmpty()) {
            if (!isValidEmail(request.email())) {
                fieldErrors.add(new FieldErrorResponse("email", "Invalid email format"));
            }
        }
    }

    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    private String generateUniqueUsername() {
        String username;
        do {
            username = "user_" + UUID.randomUUID().toString().substring(0, 8);
        } while (userRepository.existsByUsername(username));
        return username;
    }
}
