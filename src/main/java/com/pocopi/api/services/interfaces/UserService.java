package com.pocopi.api.services.interfaces;

import com.pocopi.api.dto.Auth.NewUser;
import com.pocopi.api.dto.User.User;
import com.pocopi.api.models.user.UserModel;

import java.util.List;

public interface UserService {
    List<User> getAll();
    String createUser(NewUser request);
    UserModel getUserById(int id);
    User getByUsername(String username);
    List<Integer> getAllUserIds();
}
