package com.resumebuilder.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.resumebuilder.backend.config.AppProperties;
import com.resumebuilder.backend.config.JwtConfig;
import com.resumebuilder.backend.security.TokenAuthFilter;
import com.resumebuilder.backend.security.JwtAuthFilter;
import com.resumebuilder.backend.security.JwtService;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final AppProperties appProperties;

    private final JwtConfig jwtConfig;
    private final JwtService jwtService;

    public SecurityConfig(AppProperties appProperties, JwtConfig jwtConfig, JwtService jwtService) {
        this.appProperties = appProperties;
        this.jwtConfig = jwtConfig;
        this.jwtService = jwtService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        if (!appProperties.getSecurity().isEnabled()) {
            http.csrf(csrf -> csrf.disable())
                    .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
            return http.build();
        }
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/ping",
                                "/api/profiles/**",
                                "/api/job-descriptions/**",
                                "/api/generation/**",
                                "/api/cover-letters/**",
                                "/api/ats/**",
                                "/api/ai/**",
                                "/api/auth/**",
                                "/actuator/health",
                                "/v3/api-docs/**",
                                "/swagger-ui.html",
                                "/swagger-ui/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                );

        if (jwtConfig.isEnabled()) {
            http.addFilterBefore(new JwtAuthFilter(jwtService, jwtConfig), UsernamePasswordAuthenticationFilter.class);
        } else if (appProperties.getSecurity().getToken() != null && !appProperties.getSecurity().getToken().isBlank()) {
            http.addFilterBefore(new TokenAuthFilter(appProperties), UsernamePasswordAuthenticationFilter.class);
        }
        http.httpBasic(Customizer.withDefaults());

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
