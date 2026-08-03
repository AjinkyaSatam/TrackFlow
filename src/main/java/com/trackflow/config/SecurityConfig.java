package com.trackflow.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Temporary Security Configuration for Step 1.
 *
 * <p>This is a placeholder that permits all requests so we can test
 * the application without authentication during initial setup.
 * We will replace this with full JWT authentication in Step 2.</p>
 *
 * <h3>Why not just disable security?</h3>
 * <p>Even in early development, having Spring Security on the classpath
 * and configured (even permissively) means:</p>
 * <ul>
 *   <li>CORS, CSRF, and header protections are properly configured</li>
 *   <li>The PasswordEncoder bean is ready for user registration</li>
 *   <li>The security filter chain is in place for future JWT integration</li>
 * </ul>
 *
 * <h3>Interview Question:</h3>
 * <p>"Why use BCrypt for password hashing?"</p>
 * <p>Answer: BCrypt is an adaptive hashing function. It uses a cost factor (salt rounds)
 * that makes brute-force attacks computationally expensive. Unlike MD5/SHA,
 * BCrypt is designed for passwords — it's intentionally slow.</p>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Configures the HTTP security filter chain.
     *
     * <p>Current configuration:</p>
     * <ul>
     *   <li>CSRF disabled (we use JWT, not cookies — CSRF is irrelevant)</li>
     *   <li>Stateless session (REST APIs should not maintain server-side sessions)</li>
     *   <li>All endpoints are permitted (temporary — will be locked down in Step 2)</li>
     * </ul>
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF — JWT-based auth doesn't need it
                .csrf(csrf -> csrf.disable())

                // Stateless session — no session cookies
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Permit all requests for now (will be restricted in Step 2)
                .authorizeHttpRequests(auth ->
                        auth.anyRequest().permitAll()
                );

        return http.build();
    }

    /**
     * BCrypt password encoder bean.
     *
     * <p>BCrypt automatically handles:</p>
     * <ul>
     *   <li>Salt generation (each password gets a unique salt)</li>
     *   <li>Configurable strength (default: 10 rounds)</li>
     *   <li>One-way hashing (cannot reverse to plaintext)</li>
     * </ul>
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
