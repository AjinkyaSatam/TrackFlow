package com.trackflow.service;

import com.trackflow.dto.AuthResponse;
import com.trackflow.dto.LoginRequest;
import com.trackflow.dto.RegisterRequest;
import com.trackflow.dto.TokenRefreshRequest;
import com.trackflow.entity.*;
import com.trackflow.exception.BadRequestException;
import com.trackflow.exception.DuplicateResourceException;
import com.trackflow.repository.OrganizationRepository;
import com.trackflow.repository.RefreshTokenRepository;
import com.trackflow.repository.UserRepository;
import com.trackflow.security.CustomUserDetails;
import com.trackflow.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service managing user authentication operations including registration, login, logout, and token refresh.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    /**
     * Registers a new user.
     *
     * <p>If {@code organizationName} is specified, we create a new Organization
     * and assign the registered user as its owner (using safe transactional order).</p>
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // 1. Check if email is already taken
        if (userRepository.existsByEmailAndIsDeletedFalse(request.getEmail())) {
            throw new DuplicateResourceException("User", "email", request.getEmail());
        }

        // 2. Build and save the User (initially without Organization to solve chicken-and-egg foreign keys)
        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .active(true)
                .emailVerified(false)
                .build();

        user = userRepository.save(user);

        // 3. Handle Organization creation if requested
        if (request.getOrganizationName() != null && !request.getOrganizationName().isBlank()) {
            Organization organization = Organization.builder()
                    .name(request.getOrganizationName())
                    .owner(user)
                    .build();
            organization = organizationRepository.save(organization);

            // Update user to reference their newly created organization
            user.setOrganization(organization);
            user = userRepository.save(user);
        }

        // 4. Generate Tokens
        CustomUserDetails userDetails = new CustomUserDetails(user);
        String accessToken = jwtService.generateToken(userDetails);
        RefreshToken refreshToken = createRefreshToken(user);

        return buildAuthResponse(user, accessToken, refreshToken.getToken());
    }

    /**
     * Authenticates credentials and returns JWT access & refresh tokens.
     */
    @Transactional
    public AuthResponse login(LoginRequest request) {
        // 1. Find user by email
        User user = userRepository.findByEmailAndIsDeletedFalse(request.getEmail())
                .orElseThrow(() -> new BadRequestException("Invalid email or password"));

        // 2. Validate password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadRequestException("Invalid email or password");
        }

        // 3. Check if account is active
        if (!user.isActive()) {
            throw new BadRequestException("User account is inactive. Please contact support.");
        }

        // 4. Generate fresh tokens
        CustomUserDetails userDetails = new CustomUserDetails(user);
        String accessToken = jwtService.generateToken(userDetails);

        // Revoke any existing tokens this user might have to ensure single-device or clean session security
        refreshTokenRepository.revokeAllUserTokens(user.getId());
        RefreshToken refreshToken = createRefreshToken(user);

        return buildAuthResponse(user, accessToken, refreshToken.getToken());
    }

    /**
     * Exchanges a valid refresh token for a brand new short-lived access token.
     */
    @Transactional
    public AuthResponse refreshToken(TokenRefreshRequest request) {
        RefreshToken token = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new BadRequestException("Invalid refresh token"));

        if (!token.isValid()) {
            // Revoke it to prevent reuse if someone stole it
            token.setRevoked(true);
            refreshTokenRepository.save(token);
            throw new BadRequestException("Refresh token is expired or has been revoked");
        }

        User user = token.getUser();
        CustomUserDetails userDetails = new CustomUserDetails(user);
        String accessToken = jwtService.generateToken(userDetails);

        return buildAuthResponse(user, accessToken, token.getToken());
    }

    /**
     * Logs out the user by revoking all their active refresh tokens.
     */
    @Transactional
    public void logout(Long userId) {
        refreshTokenRepository.revokeAllUserTokens(userId);
    }

    private RefreshToken createRefreshToken(User user) {
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                // Expiration matching the 7-day setting configured in application.yml
                .expiryDate(LocalDateTime.now().plusDays(7))
                .revoked(false)
                .build();
        return refreshTokenRepository.save(refreshToken);
    }

    private AuthResponse buildAuthResponse(User user, String accessToken, String refreshToken) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .userId(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .build();
    }
}
