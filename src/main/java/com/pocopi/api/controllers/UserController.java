package com.pocopi.api.controllers;

import com.pocopi.api.dto.auth.NewUser;
import com.pocopi.api.dto.user.User;
import com.pocopi.api.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        final List<User> users = userService.getAll();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{username}")
    public ResponseEntity<User> getUser(@PathVariable String username) {
        final User user = userService.getByUsername(username);
        return ResponseEntity.ok(user);
    }

    @PostMapping
    @Operation(
        summary = "Create new user",
        description = "Creates a new user and returns success message",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "User created successfully"
            )
        }
    )
    public ResponseEntity<Void> createUser(@RequestBody NewUser request) {
        userService.createUser(request);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }
}
