package com.pocopi.api.services.interfaces;

import com.pocopi.api.dto.User.SingleUserResponse;
import com.pocopi.api.models.UserModel;

import java.util.List;

public interface UserService {

    UserModel getUser(int id);
    List<SingleUserResponse> getAll();
}
