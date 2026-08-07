package com.trackflow.dto;

import com.trackflow.entity.IssuePriority;
import com.trackflow.entity.IssueType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Data Transfer Object representing an issue.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IssueDTO {

    private Long id;
    private String issueKey;
    private String title;
    private String description;
    private IssueType type;
    private IssuePriority priority;
    private String status;
    private LocalDate dueDate;
    private Double estimatedHours;
    private String labels;
    private Long reporterId;
    private String reporterName;
    private Long assigneeId;
    private String assigneeName;
    private Long projectId;
    private Long sprintId;
    private String sprintName;
    private LocalDateTime createdAt;
}
