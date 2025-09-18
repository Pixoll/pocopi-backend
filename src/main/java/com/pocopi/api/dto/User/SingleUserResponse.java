package com.pocopi.api.dto.User;

public record SingleUserResponse(
        String username,
        String name,
        String email,
        byte age
) {

}
