package com.example.demo.security;

import com.example.demo.jwt.JwtAuthFilter;
import com.example.demo.jwt.JwtService;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.http.HttpMethod;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;

import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

import org.springframework.http.HttpStatus;

@Configuration
public class SecurityConfig {

    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;

    public SecurityConfig(UserDetailsService uds, PasswordEncoder encoder) {
        this.userDetailsService = uds;
        this.passwordEncoder = encoder;
    }

    @Bean
    public JwtAuthFilter jwtAuthFilter(JwtService jwtService, UserDetailsService uds) {
        return new JwtAuthFilter(jwtService, uds);
    }

    @Bean
    public DaoAuthenticationProvider daoAuthProvider() {
        var p = new DaoAuthenticationProvider();
        p.setUserDetailsService(userDetailsService);
        p.setPasswordEncoder(passwordEncoder);
        return p;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception {
        return cfg.getAuthenticationManager();
    }

    @Bean
    public org.springframework.web.cors.CorsConfigurationSource corsConfigurationSource() {
        var cfg = new org.springframework.web.cors.CorsConfiguration();
        cfg.addAllowedOriginPattern("*");
        cfg.addAllowedMethod("*");
        cfg.addAllowedHeader("*"); // includes Authorization
        cfg.setAllowCredentials(false);
        var source = new org.springframework.web.cors.UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthFilter jwtAuthFilter) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(c -> {})
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(eh -> eh
                .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
            )
            .authorizeHttpRequests(auth -> auth
			.requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()
			.requestMatchers("/", "/index.html", "/assets/**", "/favicon.ico", "/vite.svg").permitAll()
			.requestMatchers("/api/hello", "/api/auth/**", "/actuator/**").permitAll()
			.requestMatchers("/error").permitAll()     // <-- add this
			.anyRequest().authenticated()
			)

            .authenticationProvider(daoAuthProvider())
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
