package com.pocopi.api.controllers;

import com.pocopi.api.config.auth.AuthUser;
import com.pocopi.api.config.auth.JwtUtil;
import com.pocopi.api.dto.auth.Credentials;
import com.pocopi.api.dto.auth.CredentialsUpdate;
import com.pocopi.api.dto.user.NewUser;
import com.pocopi.api.dto.auth.Token;
import com.pocopi.api.services.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication")
public class AuthController {
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtils;

    public AuthController(UserService userService, AuthenticationManager authenticationManager, JwtUtil jwtUtils) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
    }

    @PostMapping("/login")
    public ResponseEntity<Token> login(@RequestBody @Valid Credentials credentials) {
        final Authentication authentication = authenticationManager
            .authenticate(new UsernamePasswordAuthenticationToken(credentials.username(), credentials.password()));

        final AuthUser userDetails = (AuthUser) authentication.getPrincipal();
        final String token = jwtUtils.generateToken(userDetails.getId());

        return new ResponseEntity<>(new Token(token), HttpStatus.CREATED);
    }

    @PostMapping("/register")
    public ResponseEntity<Token> register(@RequestBody @Valid NewUser user) {
        userService.createUser(user);

        return login(new Credentials(user.username(), user.password()));
    }

    @PatchMapping("/credentials")
    public ResponseEntity<Void> updateCredentials(
        @RequestBody @Valid CredentialsUpdate credentialsUpdate,
        @AuthenticationPrincipal AuthUser authUser
    ) {
        userService.updateCredentials(authUser.user(), credentialsUpdate);
        return ResponseEntity.ok().build();
    }
}
