package com.trackflow.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Data Transfer Object representing a system activity log.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityLogDTO {

    private Long id;
    private String activityType;
    private String description;
    private String oldValue;
    private String newValue;
    private Long userId;
    private String userName;
    private Long projectId;
    private Long issueId;
    private LocalDateTime createdAt;
}
