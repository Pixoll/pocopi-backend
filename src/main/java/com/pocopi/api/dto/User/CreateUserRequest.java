package com.pocopi.api.dto.User;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Optional;

public record CreateUserRequest(
        Optional<String> username,
        @JsonProperty("group_id") int groupId,
        boolean anonymous,
        String name,
        String email,
        byte age,
        String password
) {
}
