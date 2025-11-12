package com.example.demo.security;

import com.example.demo.user.UserEntity;
import com.example.demo.user.UserRepository;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

/**
 *
 * Author : bhask
 * Created : 11-11-2025
 */
@Service
public class DbUserDetailsService implements UserDetailsService {
    private final UserRepository repo;
    public DbUserDetailsService(UserRepository repo){ this.repo = repo; }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity u = repo.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        // Map user_type to roles; add more later
        String role = switch (u.getUserType()) {
            case BUSINESS -> "BUSINESS";
            case RETAILER -> "RETAILER";
        };
        return org.springframework.security.core.userdetails.User
                .withUsername(u.getUsername())
                .password(u.getPasswordHash())
                .roles(role) // becomes ROLE_BUSINESS / ROLE_RETAILER
                .build();
    }
}