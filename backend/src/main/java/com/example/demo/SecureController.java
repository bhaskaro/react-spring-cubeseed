package com.example.demo;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 * Author : bhask
 * Created : 11-10-2025
 */
@RestController
@RequestMapping("/api/secure")
public class SecureController {
    
	@GetMapping("/hello")
    public Map<String,String> hello(@AuthenticationPrincipal UserDetails user){
        return Map.of("message","Hello "+user.getUsername()+" (secured)");
    }
	
	@GetMapping("/me")
    public Map<String, Object> me(Authentication auth) {
        return Map.of(
            "username", auth.getName(),
            "authorities", auth.getAuthorities()
                               .stream().map(GrantedAuthority::getAuthority)
                               .collect(Collectors.toList())
        );
    }
}
