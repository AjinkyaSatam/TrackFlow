package com.trackflow.dto;

import com.trackflow.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Data Transfer Object representing a project member's information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectMemberDTO {

    private Long id;
    private Long userId;
    private String fullName;
    private String email;
    private Role role;
    private LocalDateTime joinedAt;
}
