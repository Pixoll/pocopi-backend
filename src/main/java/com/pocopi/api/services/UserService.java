package com.pocopi.api.services;

import com.pocopi.api.dto.api.FieldError;
import com.pocopi.api.dto.auth.NewUser;
import com.pocopi.api.dto.user.User;
import com.pocopi.api.exception.MultiFieldException;
import com.pocopi.api.models.user.UserModel;
import com.pocopi.api.repositories.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<User> getAll() {
        final List<UserModel> users = userRepository.getAllUsers();
        final List<User> usersResponse = new ArrayList<>();
        for (final UserModel user : users) {
            usersResponse.add(new User(
                user.getId(),
                user.getUsername(),
                user.getName(),
                user.isAnonymous(),
                user.getEmail(),
                user.getAge()
            ));
        }
        return usersResponse;
    }

    @Transactional
    public void createUser(NewUser request) throws MultiFieldException {
        final List<FieldError> fieldErrors = new ArrayList<>();

        if (userRepository.existsByUsername(request.username())) {
            fieldErrors.add(new FieldError("username", "Username already exists"));
        }

        if (request.email() != null && !request.email().trim().isEmpty()) {
            if (userRepository.existsByEmail(request.email())) {
                fieldErrors.add(new FieldError("email", "Email already exists"));
            }
        }

        validateOnCreateUser(request, fieldErrors);
        validateFieldLengths(request, fieldErrors);

        if (!fieldErrors.isEmpty()) {
            throw new MultiFieldException("Some error in fields", fieldErrors);
        }

        try {
            final String name = (request.anonymous() || request.name() == null || request.name().trim().isEmpty())
                ? null
                : request.name();

            final String email = (request.anonymous() || request.email() == null || request.email().trim().isEmpty())
                ? null
                : request.email();

            Byte age = null;
            if (!request.anonymous() && request.age() != null && !request.age().toString().trim().isEmpty()) {
                try {
                    age = Byte.parseByte(String.valueOf(request.age()));
                } catch (NumberFormatException e) {
                    throw new RuntimeException("Invalid age format");
                }
            }

            final UserModel newUser = UserModel.builder()
                .username(request.username())
                .anonymous(request.anonymous())
                .name(name)
                .email(email)
                .age(age)
                .password(passwordEncoder.encode(request.password()))
                .build();

            userRepository.insertNewUser(
                newUser.getUsername(),
                newUser.isAnonymous(),
                newUser.getName(),
                newUser.getEmail(),
                newUser.getAge(),
                newUser.getPassword()
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to create user: " + e.getMessage());
        }
    }

    public UserModel getUserById(int id) {
        return userRepository.getUserByUserId(id);
    }

    public User getByUsername(String username) throws RuntimeException {
        if (!userRepository.existsByUsername(username)) {
            throw new RuntimeException("Username not found");
        }
        final UserModel savedUser = userRepository.findByUsername(username);
        return new User(
            savedUser.getId(),
            savedUser.getUsername(),
            savedUser.getName(),
            savedUser.isAnonymous(),
            savedUser.getEmail(),
            savedUser.getAge()
        );
    }

    public List<Integer> getAllUserIds() {
        return userRepository.getAllUserIds();
    }

    private void validateOnCreateUser(NewUser request, List<FieldError> fieldErrors) {
        if (request.anonymous()) {
            if (request.name() != null && !request.name().trim().isEmpty()) {
                fieldErrors.add(new FieldError("name", "Name must be empty for anonymous users"));
            }
            if (request.email() != null && !request.email().trim().isEmpty()) {
                fieldErrors.add(new FieldError("email", "Email must be empty for anonymous users"));
            }
            if (request.age() != null && !request.age().toString().isEmpty()) {
                fieldErrors.add(new FieldError("age", "Age must be empty for anonymous users"));
            }
        } else {
            if (request.name() == null || request.name().trim().isEmpty()) {
                fieldErrors.add(new FieldError("name", "Name is required for non-anonymous users"));
            }

            if (request.email() == null || request.email().trim().isEmpty()) {
                fieldErrors.add(new FieldError("email", "Email is required for non-anonymous users"));
            }

            if (request.age() == null || request.age().toString().trim().isEmpty()) {
                fieldErrors.add(new FieldError("age", "Age is required for non-anonymous users"));
            } else {
                try {
                    final int ageValue = Integer.parseInt(String.valueOf(request.age()));
                    if (ageValue < 1 || ageValue > 120) {
                        fieldErrors.add(new FieldError("age", "Age must be between 1 and 120"));
                    }
                } catch (NumberFormatException e) {
                    fieldErrors.add(new FieldError("age", "Age must be a valid number"));
                }
            }
        }
    }

    private void validateFieldLengths(NewUser request, List<FieldError> fieldErrors) {
        if (request.username().length() > 32) {
            fieldErrors.add(new FieldError("username", "Username cannot exceed 32 characters"));
        }
        if (request.username().trim().isEmpty()) {
            fieldErrors.add(new FieldError("username", "Username cannot be empty"));
        }

        if (request.name() != null && request.name().length() > 50) {
            fieldErrors.add(new FieldError("name", "Name cannot exceed 50 characters"));
        }
        if (request.email() != null && request.email().length() > 50) {
            fieldErrors.add(new FieldError("email", "Email cannot exceed 50 characters"));
        }

        if (request.password() != null && request.password().trim().isEmpty()) {
            fieldErrors.add(new FieldError("password", "Password cannot be empty"));
        }

        if (!request.anonymous() && request.email() != null && !request.email().trim().isEmpty()) {
            if (!isValidEmail(request.email())) {
                fieldErrors.add(new FieldError("email", "Invalid email format"));
            }
        }
    }

    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }
}