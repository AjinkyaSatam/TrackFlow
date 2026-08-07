package com.trackflow.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Data Transfer Object representing a user profile sent to clients.
 *
 * <p>Never exposes sensitive fields like hashed password strings.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {

    private Long id;
    private String fullName;
    private String email;
    private String profileImage;
    private String role;
    private boolean active;
    private boolean emailVerified;
    private Long organizationId;
    private String organizationName;
    private LocalDateTime createdAt;
}
