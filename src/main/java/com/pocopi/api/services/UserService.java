package com.pocopi.api.services;

import com.pocopi.api.dto.api.FieldError;
import com.pocopi.api.dto.user.NewAdmin;
import com.pocopi.api.dto.user.NewUser;
import com.pocopi.api.dto.user.User;
import com.pocopi.api.exception.HttpException;
import com.pocopi.api.exception.MultiFieldException;
import com.pocopi.api.mappers.ApiExceptionMapper;
import com.pocopi.api.models.config.ConfigModel;
import com.pocopi.api.models.config.PatternModel;
import com.pocopi.api.models.user.Role;
import com.pocopi.api.models.user.UserModel;
import com.pocopi.api.repositories.ConfigRepository;
import com.pocopi.api.repositories.UserRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ConfigRepository configRepository;
    private final Validator validator;
    private final ApiExceptionMapper apiExceptionMapper;

    public UserService(
        UserRepository userRepository,
        PasswordEncoder passwordEncoder,
        ConfigRepository configRepository,
        Validator validator,
        ApiExceptionMapper apiExceptionMapper
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.configRepository = configRepository;
        this.validator = validator;
        this.apiExceptionMapper = apiExceptionMapper;
    }

    public List<User> getAll() {
        final List<UserModel> users = userRepository.getAllUsers();
        return users.stream().map(user -> new User(
            user.getId(),
            user.getUsername(),
            user.isAnonymous(),
            user.getName(),
            user.getEmail(),
            user.getAge() != null ? user.getAge().intValue() : null
        )).collect(Collectors.toList());
    }

    public User getByUsername(String username) {
        final UserModel user = userRepository.findByUsername(username)
            .orElseThrow(() -> HttpException.notFound("User " + username + " not found"));

        return new User(
            user.getId(),
            user.getUsername(),
            user.isAnonymous(),
            user.getName(),
            user.getEmail(),
            user.getAge() != null ? user.getAge().intValue() : null
        );
    }

    @Transactional
    public void createUser(NewUser user) {
        final ConfigModel config = configRepository.getLastConfig();
        final boolean anonymous = config.isAnonymous();
        final PatternModel usernamePattern = config.getUsernamePattern();

        final List<FieldError> fieldErrors = new ArrayList<>();

        validateNewUser(user, fieldErrors, anonymous, usernamePattern);

        if (userRepository.existsByUsername(user.username())) {
            fieldErrors.add(new FieldError("username", "User with that username already exists"));
        }

        if (user.email() != null && userRepository.existsByEmail(user.email())) {
            fieldErrors.add(new FieldError("email", "User with that email already exists"));
        }

        if (!fieldErrors.isEmpty()) {
            throw new MultiFieldException("Invalid user", fieldErrors);
        }

        final UserModel newUser = UserModel.builder()
            .username(user.username())
            .anonymous(anonymous)
            .name(user.name())
            .email(user.email())
            .age(user.age() != null ? user.age().byteValue() : null)
            .password(passwordEncoder.encode(user.password()))
            .build();

        userRepository.save(newUser);
    }

    @Transactional
    public void createAdmin(NewAdmin admin) {
        final Set<ConstraintViolation<NewAdmin>> errors = validator.validate(admin);
        final ArrayList<FieldError> fieldErrors = new ArrayList<>(
            apiExceptionMapper.fromValidationErrors(errors).getErrors()
        );

        validateNewAdmin(admin, fieldErrors);

        if (userRepository.existsByUsername(admin.username())) {
            fieldErrors.add(new FieldError("username", "Admin with that username already exists"));
        }

        if (!fieldErrors.isEmpty()) {
            throw new MultiFieldException("Invalid admin", fieldErrors);
        }

        final UserModel newAdmin = UserModel.builder()
            .username(admin.username())
            .role(Role.ADMIN)
            .anonymous(true)
            .password(passwordEncoder.encode(admin.password()))
            .build();

        userRepository.save(newAdmin);
    }

    private void validateNewUser(
        NewUser user,
        List<FieldError> fieldErrors,
        boolean anonymous,
        PatternModel usernamePattern
    ) {
        if (user.username().contains(" ")) {
            fieldErrors.add(new FieldError("username", "Username cannot contain spaces"));
        }

        if (user.password().contains(" ")) {
            fieldErrors.add(new FieldError("password", "Password cannot contain spaces"));
        }

        if (anonymous) {
            if (user.name() != null) {
                fieldErrors.add(new FieldError("name", "Name must be empty for anonymous users"));
            }

            if (user.email() != null) {
                fieldErrors.add(new FieldError("email", "Email must be empty for anonymous users"));
            }

            if (user.age() != null) {
                fieldErrors.add(new FieldError("age", "Age must be empty for anonymous users"));
            }

            return;
        }

        if (usernamePattern != null && !usernamePattern.getPattern().matcher(user.username()).matches()) {
            fieldErrors.add(new FieldError("username", "Username is not a valid " + usernamePattern.getName()));
        }

        if (user.name() == null || user.name().isEmpty()) {
            fieldErrors.add(new FieldError("name", "Name is required for non-anonymous users"));
        }

        if (user.email() == null || user.email().isEmpty()) {
            fieldErrors.add(new FieldError("email", "Email is required for non-anonymous users"));
        }

        if (user.age() == null) {
            fieldErrors.add(new FieldError("age", "Age is required for non-anonymous users"));
        }
    }

    private void validateNewAdmin(NewAdmin admin, List<FieldError> fieldErrors) {
        if (admin.username().contains(" ")) {
            fieldErrors.add(new FieldError("username", "Username cannot contain spaces"));
        }

        if (admin.password().contains(" ")) {
            fieldErrors.add(new FieldError("password", "Password cannot contain spaces"));
        }
    }
}
