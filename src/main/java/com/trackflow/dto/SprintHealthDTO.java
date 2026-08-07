package com.trackflow.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO representing calculated Sprint Health parameters.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SprintHealthDTO {

    private Long sprintId;
    private String sprintName;
    private int healthScore; // 0 to 100
    private long totalIssues;
    private long completedIssues;
    private long openIssues;
    private long criticalBugs;
    private List<String> feedbackWarnings; // Suggestions like "Heavy workload detected on User X"
}
