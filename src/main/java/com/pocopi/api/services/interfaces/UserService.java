package com.pocopi.api.services.interfaces;

import com.pocopi.api.dto.User.CreateUserRequest;
import com.pocopi.api.dto.User.SingleUserResponse;
import com.pocopi.api.dto.User.UserSummaryResponse;
import com.pocopi.api.models.UserModel;

import java.util.List;

public interface UserService {

    List<SingleUserResponse> getAll();
    String createUser(CreateUserRequest request);
    UserModel getUserById(int id);
    List<Integer> getAllUserIds();
}
