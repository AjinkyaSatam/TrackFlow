package com.trackflow.mapper;

import com.trackflow.dto.UserDTO;
import com.trackflow.entity.User;
import org.springframework.stereotype.Component;

/**
 * Mapper utility to convert between {@link User} entity and {@link UserDTO}.
 *
 * <p>Uses manual mapping for maximum performance, type-safety, and debugging clarity.</p>
 */
@Component
public class UserMapper {

    /**
     * Converts a User entity to UserDTO.
     */
    public UserDTO toDTO(User user) {
        if (user == null) {
            return null;
        }

        Long orgId = null;
        String orgName = null;

        if (user.getOrganization() != null) {
            orgId = user.getOrganization().getId();
            orgName = user.getOrganization().getName();
        }

        return UserDTO.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .profileImage(user.getProfileImage())
                .role(user.getRole().name())
                .active(user.isActive())
                .emailVerified(user.isEmailVerified())
                .organizationId(orgId)
                .organizationName(orgName)
                .createdAt(user.getCreatedAt())
                .build();
    }
}
