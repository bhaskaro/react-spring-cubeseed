package com.example.demo.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;

@Service
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    private final SecretKey key;
    private final long jwtTtlSeconds;

    public JwtService(
            @Value("${app.jwt.secret}") String base64Secret,
            @Value("${app.jwt.ttl-seconds:3600}") long jwtTtlSeconds
    ) {
        // Base64 decode → 32+ bytes (256+ bits) required for HS256
        this.key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(base64Secret));
        this.jwtTtlSeconds = jwtTtlSeconds;
    }

    public String generate(String username, Map<String, Object> extraClaims, Collection<? extends GrantedAuthority> auths) {
        Map<String,Object> claims = new HashMap<>(extraClaims != null ? extraClaims : Map.of());
        if (auths != null) {
            claims.put("roles", auths.stream().map(GrantedAuthority::getAuthority).toArray(String[]::new));
        }
        Instant now = Instant.now();
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(jwtTtlSeconds)))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public boolean isValid(String token, String username) {
        String sub = extractUsername(token);
		log.debug("validate: sub='{}' vs user='{}'", sub, username);
        return sub != null && sub.equalsIgnoreCase(username) && !isExpired(token);
    }

    public boolean isExpired(String token) {
        Date exp = extractClaim(token, Claims::getExpiration);
        return exp == null || exp.before(new Date());
    }

    public <T> T extractClaim(String token, Function<Claims, T> resolver) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return resolver.apply(claims);
    }
}
