package com.resumebuilder.backend.security;

import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;

import com.resumebuilder.backend.config.JwtConfig;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    private final JwtConfig config;

    public JwtService(JwtConfig config) {
        this.config = config;
    }

    public String generateToken(String subject, List<String> roles) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + config.getExpirationSeconds() * 1000);
        return Jwts.builder()
                .subject(subject)
                .claim("roles", roles)
                .issuedAt(now)
                .expiration(exp)
                .signWith(Keys.hmacShaKeyFor(config.getSecret().getBytes()))
                .compact();
    }

    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(config.getSecret().getBytes()))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
