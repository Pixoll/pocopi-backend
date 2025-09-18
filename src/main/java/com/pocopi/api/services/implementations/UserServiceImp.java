package com.pocopi.api.services.implementations;

import com.pocopi.api.dto.User.SingleUserResponse;
import com.pocopi.api.models.UserModel;
import com.pocopi.api.repositories.UserRepository;
import com.pocopi.api.services.interfaces.UserService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserServiceImp implements UserService {
    private final UserRepository userRepository;

    public UserServiceImp(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserModel getUser(int id) {
        return null;
    }

    @Override
    public List<SingleUserResponse> getAll() {
        List<UserModel> users = userRepository.getAllUsers();
        List<SingleUserResponse> usersResponse = new ArrayList<>();
        for (UserModel user : users) {
            usersResponse.add(new SingleUserResponse(user.getUsername(), user.getName(), user.getEmail(), user.getAge()));
        }
        return usersResponse;
    }
}
