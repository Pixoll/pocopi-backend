package com.pocopi.api.services.implementations;

import com.pocopi.api.dto.Auth.NewUser;
import com.pocopi.api.dto.User.User;
import com.pocopi.api.dto.api.FieldErrorResponse;
import com.pocopi.api.exception.MultiFieldException;
import com.pocopi.api.models.user.UserModel;
import com.pocopi.api.repositories.UserRepository;
import com.pocopi.api.services.interfaces.UserService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserServiceImp implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImp(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
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
    public String createUser(NewUser request) throws MultiFieldException {
        List<FieldErrorResponse> fieldErrors = new ArrayList<>();

        if (userRepository.existsByUsername(request.username())) {
            fieldErrors.add(new FieldErrorResponse("username", "Username already exists"));
        }

        if (request.email() != null && !request.email().trim().isEmpty()) {
            if (userRepository.existsByEmail(request.email())) {
                fieldErrors.add(new FieldErrorResponse("email", "Email already exists"));
            }
        }

        validateOnCreateUser(request, fieldErrors);
        validateFieldLengths(request, fieldErrors);

        if (!fieldErrors.isEmpty()) {
            throw new MultiFieldException("Some error in fields", fieldErrors);
        }

        try {
            UserModel newUser = UserModel.builder()
                .username(request.username())
                .anonymous(request.anonymous())
                .name(request.name())
                .email(request.email())
                .age(request.age())
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

    private void validateOnCreateUser(NewUser request, List<FieldErrorResponse> fieldErrors) {
        if (request.anonymous()) {
            if (request.email() != null && !request.email().trim().isEmpty()) {
                fieldErrors.add(new FieldErrorResponse("email", "Email must be null for anonymous users"));
            }
            return;
        }

        if (request.name() == null || request.name().trim().isEmpty()) {
            fieldErrors.add(new FieldErrorResponse("name", "Name is required for non-anonymous users"));
        }

        if (request.email() == null || request.email().trim().isEmpty()) {
            fieldErrors.add(new FieldErrorResponse("email", "Email is required for non-anonymous users"));
        }

        if (request.age() < 1 || request.age() > 120) {
            fieldErrors.add(new FieldErrorResponse("age", "Invalid age"));
        }
    }

    private void validateFieldLengths(NewUser request, List<FieldErrorResponse> fieldErrors) {
        if (request.username().length() > 32) {
            fieldErrors.add(new FieldErrorResponse("username", "Username cannot exceed 32 characters"));
        }
        if (request.username().trim().isEmpty()) {
            fieldErrors.add(new FieldErrorResponse("username", "Username cannot be empty"));
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
}
