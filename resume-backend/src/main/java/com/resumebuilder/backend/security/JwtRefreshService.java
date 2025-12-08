package com.resumebuilder.backend.security;

import java.time.OffsetDateTime;
import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;

import com.resumebuilder.backend.config.JwtConfig;
import com.resumebuilder.backend.domain.RefreshToken;
import com.resumebuilder.backend.domain.UserAccount;
import com.resumebuilder.backend.repository.RefreshTokenRepository;
import com.resumebuilder.backend.repository.UserAccountRepository;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtRefreshService {

    private final JwtConfig config;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserAccountRepository userAccountRepository;

    public JwtRefreshService(JwtConfig config, RefreshTokenRepository refreshTokenRepository,
            UserAccountRepository userAccountRepository) {
        this.config = config;
        this.refreshTokenRepository = refreshTokenRepository;
        this.userAccountRepository = userAccountRepository;
    }

    public String generateRefreshToken(String subject, Long userId) {
        Date now = new Date();
        // refresh token lasts 7 days by default
        Date exp = new Date(now.getTime() + 7 * 24 * 3600 * 1000);
        String token = Jwts.builder()
                .subject(subject)
                .issuer(config.getIssuer())
                .issuedAt(now)
                .expiration(exp)
                .signWith(Keys.hmacShaKeyFor(config.getSecret().getBytes()))
                .compact();
        UserAccount user = userAccountRepository.findById(userId).orElseThrow();
        RefreshToken rt = new RefreshToken();
        rt.setToken(token);
        rt.setUser(user);
        rt.setExpiresAt(OffsetDateTime.ofInstant(exp.toInstant(), java.time.ZoneOffset.UTC));
        rt.setRevoked(false);
        refreshTokenRepository.save(rt);
        return token;
    }

    public Claims parse(String token) {
        return Jwts.parser()
                .requireIssuer(config.getIssuer())
                .verifyWith(Keys.hmacShaKeyFor(config.getSecret().getBytes()))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String rotateAccessFromRefresh(String refreshToken, List<String> roles) {
        if (!isValid(refreshToken)) {
            throw new IllegalStateException("Invalid or revoked refresh token");
        }
        Claims claims = parse(refreshToken);
        String subject = claims.getSubject();
        return Jwts.builder()
                .subject(subject)
                .claim("roles", roles)
                .issuer(config.getIssuer())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + config.getExpirationSeconds() * 1000))
                .signWith(Keys.hmacShaKeyFor(config.getSecret().getBytes()))
                .compact();
    }

    public boolean isValid(String refreshToken) {
        return refreshTokenRepository.findByToken(refreshToken)
                .filter(rt -> !rt.isRevoked())
                .filter(rt -> rt.getExpiresAt().isAfter(OffsetDateTime.now()))
                .isPresent();
    }

    public void revoke(String refreshToken) {
        refreshTokenRepository.findByToken(refreshToken).ifPresent(rt -> {
            rt.setRevoked(true);
            refreshTokenRepository.save(rt);
        });
    }
}
