package com.pocopi.api.services.interfaces;

import com.pocopi.api.dto.User.CreateUserRequest;
import com.pocopi.api.dto.User.SingleUserResponse;

import java.util.List;

public interface UserService {

    List<SingleUserResponse> getAll();
    String createUser(CreateUserRequest request);
}
