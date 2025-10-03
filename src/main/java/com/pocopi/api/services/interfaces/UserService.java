package com.pocopi.api.services.interfaces;

import com.pocopi.api.dto.User.CreateUserRequest;
import com.pocopi.api.dto.User.User;
import com.pocopi.api.models.UserModel;

import java.util.List;

public interface UserService {

    List<User> getAll();
    String createUser(CreateUserRequest request);
    UserModel getUserById(int id);
    List<Integer> getAllUserIds();
}
