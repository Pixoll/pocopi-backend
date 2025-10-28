package com.pocopi.api.controllers;

import com.pocopi.api.dto.api.ApiHttpError;
import com.pocopi.api.dto.auth.NewUser;
import com.pocopi.api.dto.user.User;
import com.pocopi.api.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
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

    @GetMapping("/all")
    public ResponseEntity<List<User>> getAllUsers() {
        final List<User> response = userService.getAll();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/{username}")
    public ResponseEntity<User> getUser(
        @PathVariable String username
    ) {
        final User response = userService.getByUsername(username);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping
    @Operation(
        summary = "Create new user",
        description = "Creates a new user and returns success message",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "User created successfully"
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Validation error",
                content = @Content(schema = @Schema(implementation = ApiHttpError.class))
            )
        }
    )
    public ResponseEntity<Void> createUser(@RequestBody NewUser request) {
        userService.createUser(request);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }
}
