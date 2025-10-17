package com.example.backend.service;

import com.example.backend.model.User;
import com.example.backend.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with username: " + username));

    // Map the user's role string (e.g., "ROLE_ADMIN" or a comma-separated list) to GrantedAuthority
    List<SimpleGrantedAuthority> authorities = Collections.emptyList();
    if (user.getRole() != null && !user.getRole().trim().isEmpty()) {
        authorities = java.util.Arrays.stream(user.getRole().split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toList());
    }

    return new org.springframework.security.core.userdetails.User(user.getUsername(),
        user.getPassword(),
        authorities);
    }
}
