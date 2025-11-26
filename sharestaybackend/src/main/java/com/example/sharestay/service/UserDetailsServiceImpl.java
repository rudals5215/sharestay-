package com.example.sharestay.service;

import com.example.sharestay.entity.User;
import com.example.sharestay.repository.UserRepository;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.userdetails.User.UserBuilder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> userOpt = userRepository.findByUsername(username);

        if (userOpt.isEmpty()) {
            throw new UsernameNotFoundException("User not found.");
        }

        User currentUser = userOpt.get();

        // ⭐ 핵심: 밴된 경우 인증 차단
        if (currentUser.isBanned()) {
            throw new DisabledException("User is banned.");
        }

        UserBuilder builder = org.springframework.security.core.userdetails.User.withUsername(username);
        builder.password(currentUser.getPassword());
        builder.roles(currentUser.getRole());

        return builder.build();
    }
}
