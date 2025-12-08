package com.resumebuilder.backend.web;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.resumebuilder.backend.config.AppProperties;
import com.resumebuilder.backend.config.JwtConfig;
import com.resumebuilder.backend.repository.UserAccountRepository;
import com.resumebuilder.backend.security.JwtRefreshService;
import com.resumebuilder.backend.security.JwtService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

@RestController
@RequestMapping("/api/auth")
@Validated
public class AuthController {

    private final AppProperties appProperties;
    private final JwtService jwtService;
    private final JwtRefreshService jwtRefreshService;
    private final JwtConfig jwtConfig;
    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(AppProperties appProperties,
            JwtService jwtService,
            JwtRefreshService jwtRefreshService,
            JwtConfig jwtConfig,
            UserAccountRepository userAccountRepository,
            PasswordEncoder passwordEncoder) {
        this.appProperties = appProperties;
        this.jwtService = jwtService;
        this.jwtRefreshService = jwtRefreshService;
        this.jwtConfig = jwtConfig;
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public static class AuthRequest {
        @NotBlank
        public String username;
        @NotBlank
        public String password;
    }

    public static class TokenResponse {
        public String accessToken;
        public String refreshToken;
        public long expiresIn;
        public String tokenType = "Bearer";
    }

    public static class RefreshRequest {
        @NotBlank
        public String refreshToken;
    }

    public static class LogoutRequest {
        @NotBlank
        public String refreshToken;
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody AuthRequest request) {
        if (!jwtConfig.isEnabled()) {
            return ResponseEntity.badRequest().build();
        }
        var user = userAccountRepository.findByUsername(request.username)
                .orElse(null);
        if (user == null || !passwordEncoder.matches(request.password, user.getPassword())) {
            return ResponseEntity.status(401).build();
        }
        List<String> roles = user.getRoles().stream().toList();
        TokenResponse resp = new TokenResponse();
        resp.accessToken = jwtService.generateToken(request.username, roles);
        resp.refreshToken = jwtRefreshService.generateRefreshToken(request.username, user.getId());
        resp.expiresIn = jwtConfig.getExpirationSeconds();
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        if (!jwtConfig.isEnabled()) {
            return ResponseEntity.badRequest().build();
        }
        List<String> roles = appProperties.getSecurity().getRoles();
        String access = jwtRefreshService.rotateAccessFromRefresh(request.refreshToken, roles);
        TokenResponse resp = new TokenResponse();
        resp.accessToken = access;
        resp.refreshToken = request.refreshToken;
        resp.expiresIn = jwtConfig.getExpirationSeconds();
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody LogoutRequest request) {
        if (!jwtConfig.isEnabled()) {
            return ResponseEntity.badRequest().build();
        }
        jwtRefreshService.revoke(request.refreshToken);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(@AuthenticationPrincipal org.springframework.security.core.userdetails.User user) {
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(java.util.Map.of(
                "username", user.getUsername(),
                "roles", user.getAuthorities().stream().map(Object::toString).toList()
        ));
    }
}
