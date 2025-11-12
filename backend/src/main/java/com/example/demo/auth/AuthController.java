package com.example.demo.auth;

import com.example.demo.jwt.JwtService;
import jakarta.validation.Valid;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authManager;
    private final UserDetailsService uds;
    private final JwtService jwt;

    public AuthController(AuthenticationManager authManager, UserDetailsService uds, JwtService jwt) {
        this.authManager = authManager;
        this.uds = uds;
        this.jwt = jwt;
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody AuthRequest req) {
        authManager.authenticate(
            new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword()));
        var user = uds.loadUserByUsername(req.getUsername());
        String token = jwt.generate(user.getUsername(), java.util.Map.of(), user.getAuthorities());
        return new AuthResponse(token);
    }
}
