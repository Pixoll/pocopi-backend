package com.pocopi.api.controllers;

import com.pocopi.api.dto.User.CreateUserRequest;
import com.pocopi.api.dto.User.SingleUserResponse;
import com.pocopi.api.services.interfaces.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/all")
    public ResponseEntity<List<SingleUserResponse>> getAllUsers() {
        List<SingleUserResponse> response = userService.getAll();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    @PostMapping
    public ResponseEntity<String> createUser(@RequestBody CreateUserRequest request) {
        String response = userService.createUser(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}
