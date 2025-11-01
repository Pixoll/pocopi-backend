package com.pocopi.api.dto.auth;

import com.pocopi.api.models.user.UserModel;
import io.swagger.v3.oas.annotations.media.Schema;

public record NewUser(
    @Schema(
        requiredMode = Schema.RequiredMode.REQUIRED,
        minLength = UserModel.USERNAME_MIN_LEN,
        maxLength = UserModel.USERNAME_MAX_LEN
    )
    String username,

    @Schema(
        requiredMode = Schema.RequiredMode.NOT_REQUIRED,
        minLength = UserModel.NAME_MIN_LEN,
        maxLength = UserModel.NAME_MIN_LEN
    )
    String name,

    @Schema(
        requiredMode = Schema.RequiredMode.NOT_REQUIRED,
        format = "email",
        minLength = UserModel.EMAIL_MIN_LEN,
        maxLength = UserModel.EMAIL_MIN_LEN
    )
    String email,

    @Schema(
        requiredMode = Schema.RequiredMode.NOT_REQUIRED,
        minimum = UserModel.AGE_MIN_STR,
        maximum = UserModel.AGE_MAX_STR
    )
    Integer age,

    @Schema(
        requiredMode = Schema.RequiredMode.REQUIRED,
        minLength = UserModel.PASSWORD_MIN_LEN,
        maxLength = UserModel.PASSWORD_MAX_LEN
    )
    String password
) {
    public NewUser {
        if (username != null) {
            username = username.trim();
        }

        if (name != null) {
            name = name.trim();
        }

        if (email != null) {
            email = email.trim();
        }

        if (password != null) {
            password = password.trim();
        }
    }
}
