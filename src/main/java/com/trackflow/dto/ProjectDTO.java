package com.trackflow.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Data Transfer Object representing a project.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectDTO {

    private Long id;
    private String name;
    private String description;
    private LocalDate deadline;
    private String status;
    private String repositoryUrl;
    private String projectKey;
    private Long organizationId;
    private Long createdById;
    private String createdByName;
    private LocalDateTime createdAt;
}
