package com.pocopi.api.config.auth;

import com.pocopi.api.models.user.UserModel;
import com.pocopi.api.repositories.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AuthUserService implements UserDetailsService {
    private final UserRepository userRepository;

    public AuthUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String usernameOrId) throws UsernameNotFoundException {
        UserModel user;

        try {
            user = userRepository.findById(Integer.parseInt(usernameOrId))
                .orElseThrow(() -> new UsernameNotFoundException("Bad credentials"));
        } catch (NumberFormatException ignored) {
            user = userRepository.findByUsername(usernameOrId)
                .orElseThrow(() -> new UsernameNotFoundException("Bad credentials"));
        }

        return new AuthUser(user);
    }
}
