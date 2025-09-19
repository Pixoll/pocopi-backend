package com.pocopi.api.dto.User;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CreateUserRequest(
        String username,
        @JsonProperty("group_id") int groupId,
        boolean anonymous,
        String name,
        String email,
        byte age,
        String password
) {
}
