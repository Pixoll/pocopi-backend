package com.pocopi.api.services.implementations;

import com.pocopi.api.dto.User.CreateUserRequest;
import com.pocopi.api.dto.User.SingleUserResponse;
import com.pocopi.api.models.TestGroupModel;
import com.pocopi.api.models.UserModel;
import com.pocopi.api.repositories.UserRepository;
import com.pocopi.api.services.interfaces.TestGroupService;
import com.pocopi.api.services.interfaces.UserService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserServiceImp implements UserService {
    private final UserRepository userRepository;
    private final TestGroupService testGroupService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImp(UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          TestGroupService testGroupService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.testGroupService = testGroupService;
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

    @Override
    @Transactional
    public String createUser(CreateUserRequest request) {
        TestGroupModel group = testGroupService.getTestGroup(request.groupId());
        System.out.println(passwordEncoder.encode(request.password()));

        UserModel newUser = UserModel.builder()
                .group(group)
                .username(request.username())
                .anonymous(request.anonymous())
                .name(request.name())
                .email(request.email())
                .age(request.age())
                .password(passwordEncoder.encode(request.password()))
                .build();

        userRepository.insertNewUser(
                newUser.getUsername(),
                newUser.getGroup().getId(),
                newUser.isAnonymous(),
                newUser.getName(),
                newUser.getEmail(),
                newUser.getAge(),
                newUser.getPassword()
        );
        return "User created successfully";
    }
}
