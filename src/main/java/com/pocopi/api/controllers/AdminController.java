package com.pocopi.api.controllers;

import com.pocopi.api.config.auth.AuthUser;
import com.pocopi.api.dto.user.User;
import com.pocopi.api.models.user.UserModel;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admins")
public class AdminController {
    @PostMapping("/me")
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
}
