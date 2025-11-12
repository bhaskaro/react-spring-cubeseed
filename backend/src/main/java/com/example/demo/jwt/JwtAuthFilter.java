package com.example.demo.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);

    private static final Set<String> EXACT_IGNORES = Set.of(
        "/", "/index.html", "/favicon.ico", "/vite.svg",
        "/api/hello",
        "/actuator/health", "/actuator/info"
    );

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public JwtAuthFilter(JwtService jwtService, UserDetailsService uds) {
        this.jwtService = jwtService;
        this.userDetailsService = uds;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest req) {
        String p = req.getServletPath();
        if (EXACT_IGNORES.contains(p)) return true;
        if (p.startsWith("/assets/")) return true;
        if (p.startsWith("/api/auth/")) return true; // only auth endpoints skipped
        if (p.startsWith("/actuator/")) return true; // public actuator in this app
        return false;
    }


	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
		throws ServletException, IOException {

	  try {
		final String path = request.getServletPath();
		final String authHeader = request.getHeader("Authorization");

		if (authHeader != null && authHeader.startsWith("Bearer ")
			&& SecurityContextHolder.getContext().getAuthentication() == null) {

		  final String token = authHeader.substring(7).trim();
		  final String sub = jwtService.extractUsername(token);
		  log.debug("JWT filter: path={}, sub(from token)={}", path, sub);

		  if (sub != null) {
			UserDetails user = userDetailsService.loadUserByUsername(sub);
			log.debug("JWT filter: userDetails.username={}", user.getUsername());

			// If your JwtService.isValid expects (token, String username):
			boolean ok = jwtService.isValid(token, user.getUsername());
			log.debug("JWT filter: isValid(token, user.username) -> {}", ok);

			if (ok) {
			  var at = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
			  at.setDetails(new org.springframework.security.web.authentication.WebAuthenticationDetailsSource().buildDetails(request));
			  SecurityContextHolder.getContext().setAuthentication(at);
			  log.debug("JWT filter: SecurityContext populated for {}", user.getUsername());
			}
		  }
		} else {
		  log.debug("JWT filter skipped: path={}, headerPresent={}", path, authHeader != null);
		}
	  } catch (Exception e) {
		log.warn("JWT filter error on {}: {}", request.getServletPath(), e.toString());
	  }

	  chain.doFilter(request, response);
	}

}
