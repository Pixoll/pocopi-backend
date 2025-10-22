package com.pocopi.api.controllers;

import com.pocopi.api.config.jwt.JwtUtil;
import com.pocopi.api.dto.auth.Credentials;
import com.pocopi.api.dto.auth.NewUser;
import com.pocopi.api.dto.auth.Token;
import com.pocopi.api.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtils;

    @Autowired
    public AuthController(
        UserService userService,
        AuthenticationManager authenticationManager,
        JwtUtil jwtUtils
    ) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
    }

    @PostMapping("/login")
    public ResponseEntity<Token> login(@RequestBody Credentials credentials) {
        final Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                credentials.username(),
                credentials.password()
            )
        );

        final UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        final String token = jwtUtils.generateToken(userDetails.getUsername());

        return new ResponseEntity<>(new Token(token), HttpStatus.OK);
    }

    @PostMapping("/register")
    public ResponseEntity<Token> register(@RequestBody NewUser user) {
        userService.createUser(user);

        return this.login(new Credentials(user.username(), user.password()));
    }
}
