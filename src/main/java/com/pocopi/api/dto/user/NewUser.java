package com.pocopi.api.dto.user;

import com.pocopi.api.models.user.UserModel;
import jakarta.validation.constraints.*;

public record NewUser(
    @NotNull
    @Size(min = UserModel.USERNAME_MIN_LEN, max = UserModel.USERNAME_MAX_LEN)
    String username,

    @Size(min = UserModel.NAME_MIN_LEN, max = UserModel.NAME_MAX_LEN)
    String name,

    @Email
    @Size(min = UserModel.EMAIL_MIN_LEN, max = UserModel.EMAIL_MAX_LEN)
    String email,

    @Min(UserModel.AGE_MIN)
    @Max(UserModel.AGE_MAX)
    Integer age,

    @NotNull
    @Size(min = UserModel.PASSWORD_MIN_LEN, max = UserModel.PASSWORD_MAX_LEN)
    String password
) {
}
