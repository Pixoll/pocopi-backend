package com.pocopi.api.dto.user;

import com.pocopi.api.models.user.UserModel;
import jakarta.validation.constraints.*;

public record NewAdmin(
    @NotNull
    @Size(min = UserModel.USERNAME_MIN_LEN, max = UserModel.USERNAME_MAX_LEN)
    String username,

    @NotNull
    @Size(min = UserModel.PASSWORD_MIN_LEN, max = UserModel.PASSWORD_MAX_LEN)
    String password
) {
}
