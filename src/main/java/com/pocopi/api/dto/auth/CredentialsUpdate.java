package com.pocopi.api.dto.auth;

import com.pocopi.api.models.user.UserModel;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CredentialsUpdate(
    @NotNull
    @Size(min = UserModel.USERNAME_MIN_LEN, max = UserModel.USERNAME_MAX_LEN)
    String oldUsername,

    @NotNull
    @Size(min = UserModel.USERNAME_MIN_LEN, max = UserModel.USERNAME_MAX_LEN)
    String newUsername,

    @NotNull
    @Size(min = UserModel.PASSWORD_MIN_LEN, max = UserModel.PASSWORD_MAX_LEN)
    String oldPassword,

    @NotNull
    @Size(min = UserModel.PASSWORD_MIN_LEN, max = UserModel.PASSWORD_MAX_LEN)
    String newPassword,

    @NotNull
    @Size(min = UserModel.PASSWORD_MIN_LEN, max = UserModel.PASSWORD_MAX_LEN)
    String confirmNewPassword
) {
}
