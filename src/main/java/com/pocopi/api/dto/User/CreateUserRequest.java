package com.pocopi.api.dto.User;
import java.util.Optional;

public record CreateUserRequest(
        Optional<String> username,
        int groupId,
        boolean anonymous,
        String name,
        String email,
        int age,
        String password
) {
}
