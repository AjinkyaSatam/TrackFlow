package com.trackflow.service;

import com.trackflow.dto.AuthResponse;
import com.trackflow.dto.LoginRequest;
import com.trackflow.dto.RegisterRequest;
import com.trackflow.entity.Role;
import com.trackflow.entity.User;
import com.trackflow.entity.RefreshToken;
import com.trackflow.exception.BadRequestException;
import com.trackflow.repository.OrganizationRepository;
import com.trackflow.repository.RefreshTokenRepository;
import com.trackflow.repository.UserRepository;
import com.trackflow.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link AuthService} using Mockito to mock repository interactions.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private RefreshToken testToken;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .fullName("John Doe")
                .email("john@company.com")
                .password("hashed_password")
                .role(Role.DEVELOPER)
                .active(true)
                .build();

        testToken = RefreshToken.builder()
                .id(1L)
                .token("unique-refresh-uuid")
                .expiryDate(LocalDateTime.now().plusDays(7))
                .user(testUser)
                .revoked(false)
                .build();
    }

    @Test
    void register_ShouldSaveUserAndReturnAuthResponse() {
        // Arrange
        RegisterRequest request = RegisterRequest.builder()
                .fullName("John Doe")
                .email("john@company.com")
                .password("SecurePass123!")
                .role(Role.DEVELOPER)
                .build();

        when(userRepository.existsByEmailAndIsDeletedFalse(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("hashed_password");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(jwtService.generateToken(any())).thenReturn("access_token");
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(testToken);

        // Act
        AuthResponse response = authService.register(request);

        // Assert
        assertNotNull(response);
        assertEquals("access_token", response.getAccessToken());
        assertEquals("unique-refresh-uuid", response.getRefreshToken());
        assertEquals("john@company.com", response.getEmail());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void login_WithValidCredentials_ShouldReturnAuthResponse() {
        // Arrange
        LoginRequest request = LoginRequest.builder()
                .email("john@company.com")
                .password("SecurePass123!")
                .build();

        when(userRepository.findByEmailAndIsDeletedFalse(request.getEmail())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(request.getPassword(), testUser.getPassword())).thenReturn(true);
        when(jwtService.generateToken(any())).thenReturn("access_token");
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(testToken);

        // Act
        AuthResponse response = authService.login(request);

        // Assert
        assertNotNull(response);
        assertEquals("access_token", response.getAccessToken());
        assertEquals("john@company.com", response.getEmail());
        verify(refreshTokenRepository, times(1)).revokeAllUserTokens(testUser.getId());
    }

    @Test
    void login_WithInvalidPassword_ShouldThrowBadRequestException() {
        // Arrange
        LoginRequest request = LoginRequest.builder()
                .email("john@company.com")
                .password("WrongPass!")
                .build();

        when(userRepository.findByEmailAndIsDeletedFalse(request.getEmail())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(request.getPassword(), testUser.getPassword())).thenReturn(false);

        // Act & Assert
        assertThrows(BadRequestException.class, () -> authService.login(request));
        verify(jwtService, never()).generateToken(any());
    }
}
