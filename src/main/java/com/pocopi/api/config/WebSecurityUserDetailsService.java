package com.pocopi.api.config;

import com.pocopi.api.models.user.UserModel;
import com.pocopi.api.repositories.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WebSecurityUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    public WebSecurityUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        final UserModel user = userRepository.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("Bad credentials");
        }

        return new User(
            user.getUsername(),
            user.getPassword(),
            List.of(new SimpleGrantedAuthority(user.getRole().getName()))
        );
    }
}
