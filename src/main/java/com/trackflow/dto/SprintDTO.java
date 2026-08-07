package com.trackflow.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Data Transfer Object representing a sprint.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SprintDTO {

    private Long id;
    private String name;
    private String goal;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private Long projectId;
    private LocalDateTime createdAt;
}
