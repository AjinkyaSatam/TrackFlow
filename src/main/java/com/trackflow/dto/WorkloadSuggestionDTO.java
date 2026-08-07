package com.trackflow.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO representing workload and suggestion metrics for balancing sprint tasks.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkloadSuggestionDTO {

    private Long developerId;
    private String developerName;
    private long activeIssuesCount;
    private double totalEstimatedHours;
    private String workloadStatus; // "LIGHT", "MODERATE", "HEAVY"
}
