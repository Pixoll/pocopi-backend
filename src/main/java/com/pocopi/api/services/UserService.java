package com.pocopi.api.services;

import com.pocopi.api.dto.api.FieldError;
import com.pocopi.api.dto.auth.NewUser;
import com.pocopi.api.dto.user.User;
import com.pocopi.api.exception.HttpException;
import com.pocopi.api.exception.MultiFieldException;
import com.pocopi.api.models.user.Role;
import com.pocopi.api.models.user.UserModel;
import com.pocopi.api.repositories.ConfigRepository;
import com.pocopi.api.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {
    private static final String EMAIL_REGEX =
        "(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\""
        + "(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])"
        + "*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:"
        + "(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}"
        + "(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:"
        + "(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)])";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ConfigRepository configRepository;

    @Autowired
    public UserService(
        UserRepository userRepository,
        PasswordEncoder passwordEncoder,
        ConfigRepository configRepository
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.configRepository = configRepository;
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
        final UserModel user = userRepository.findByUsername(username);
        if (user == null) {
            throw HttpException.notFound("User not found");
        }

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
        final boolean anonymous = configRepository.findLastConfig().isAnonymous();

        final List<FieldError> fieldErrors = new ArrayList<>();

        validateNewUser(user, fieldErrors, anonymous);

        if (user.username() != null
            && !user.username().isEmpty()
            && userRepository.existsByUsername(user.username())
        ) {
            fieldErrors.add(new FieldError("username", "User with that username already exists"));
        }

        if (user.email() != null
            && !user.email().isEmpty()
            && userRepository.existsByEmail(user.email())
        ) {
            fieldErrors.add(new FieldError("email", "User with that email already exists"));
        }

        if (!fieldErrors.isEmpty()) {
            throw new MultiFieldException("Invalid user", fieldErrors);
        }

        final UserModel newUser = UserModel.builder()
            .username(user.username())
            .role(Role.USER)
            .anonymous(anonymous)
            .name(user.name())
            .email(user.email())
            .age(user.age() != null ? user.age().byteValue() : null)
            .password(passwordEncoder.encode(user.password()))
            .build();

        userRepository.save(newUser);
    }

    private void validateNewUser(NewUser user, List<FieldError> fieldErrors, boolean anonymous) {
        if (user.username() == null || user.username().isEmpty()) {
            fieldErrors.add(new FieldError("username", "Username is required"));
        } else {
            final String username = user.username();

            if (username.contains(" ")) {
                fieldErrors.add(new FieldError("username", "Username cannot contain spaces"));
            }

            // noinspection SizeReplaceableByIsEmpty,ConstantValue
            if (username.length() < UserModel.USERNAME_MIN_LEN || username.length() > UserModel.USERNAME_MAX_LEN) {
                fieldErrors.add(new FieldError(
                    "username",
                    "Username length must be between "
                    + UserModel.USERNAME_MIN_LEN
                    + " and "
                    + UserModel.USERNAME_MAX_LEN
                    + " characters"
                ));
            }
        }

        if (user.password() == null || user.password().isEmpty()) {
            fieldErrors.add(new FieldError("password", "Password is required"));
        } else {
            final String password = user.password();

            if (password.contains(" ")) {
                fieldErrors.add(new FieldError("password", "Password cannot contain spaces"));
            }

            if (password.length() < UserModel.PASSWORD_MIN_LEN || password.length() > UserModel.PASSWORD_MAX_LEN) {
                fieldErrors.add(new FieldError(
                    "password",
                    "Password length must be between "
                    + UserModel.PASSWORD_MIN_LEN
                    + " and "
                    + UserModel.PASSWORD_MAX_LEN
                    + " characters"
                ));
            }
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

        if (user.name() == null || user.name().isEmpty()) {
            fieldErrors.add(new FieldError("name", "Name is required for non-anonymous users"));
        } else {
            // noinspection SizeReplaceableByIsEmpty,ConstantValue
            if (user.name().length() < UserModel.NAME_MIN_LEN || user.name().length() > UserModel.NAME_MAX_LEN) {
                fieldErrors.add(new FieldError(
                    "name",
                    "Name length must be between "
                    + UserModel.NAME_MIN_LEN
                    + " and "
                    + UserModel.NAME_MAX_LEN
                    + " characters"
                ));
            }
        }

        if (user.email() == null || user.email().isEmpty()) {
            fieldErrors.add(new FieldError("email", "Email is required for non-anonymous users"));
        } else {
            // noinspection SizeReplaceableByIsEmpty,ConstantValue
            if (user.email().length() < UserModel.EMAIL_MIN_LEN || user.email().length() > UserModel.EMAIL_MAX_LEN) {
                fieldErrors.add(new FieldError(
                    "email",
                    "Email length must be between "
                    + UserModel.EMAIL_MIN_LEN
                    + " and "
                    + UserModel.EMAIL_MAX_LEN
                    + " characters"
                ));
            }

            if (!user.email().matches(EMAIL_REGEX)) {
                fieldErrors.add(new FieldError("email", "Invalid email"));
            }
        }

        if (user.age() == null) {
            fieldErrors.add(new FieldError("age", "Age is required for non-anonymous users"));
        } else if (user.age() < UserModel.AGE_MIN || user.age() > UserModel.AGE_MAX) {
            fieldErrors.add(new FieldError(
                "age",
                "Age must be between " + UserModel.AGE_MIN + " and " + UserModel.AGE_MAX
            ));
        }
    }
}