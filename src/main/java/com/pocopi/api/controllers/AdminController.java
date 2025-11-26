package com.pocopi.api.controllers;

import com.pocopi.api.config.auth.AuthUser;
import com.pocopi.api.dto.user.NewAdmin;
import com.pocopi.api.dto.user.User;
import com.pocopi.api.models.user.UserModel;
import com.pocopi.api.services.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admins")
public class AdminController {
    private final UserService userService;

    public AdminController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<User>> getAllAdmins() {
        final List<User> admins = userService.getAllAdmins();
        return ResponseEntity.ok(admins);
    }

    @GetMapping("/me")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<User> getCurrentAdmin(@AuthenticationPrincipal AuthUser authUser) {
        final UserModel userModel = authUser.user();
        final User user = new User(
            userModel.getId(),
            userModel.getUsername(),
            userModel.isAnonymous(),
            userModel.getName(),
            userModel.getEmail(),
            userModel.getAge() != null ? userModel.getAge().intValue() : null
        );

        return ResponseEntity.ok(user);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> createAdmin(@RequestBody @Valid NewAdmin newAdmin) {
        userService.createAdmin(newAdmin);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }
}
