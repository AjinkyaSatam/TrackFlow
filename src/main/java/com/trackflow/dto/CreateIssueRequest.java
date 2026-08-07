package com.trackflow.dto;

import com.trackflow.entity.IssuePriority;
import com.trackflow.entity.IssueType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Request payload for creating a new issue.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateIssueRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must be less than 200 characters")
    private String title;

    private String description;

    @NotNull(message = "Issue type is required")
    private IssueType type;

    @NotNull(message = "Issue priority is required")
    private IssuePriority priority;

    private LocalDate dueDate;

    private Double estimatedHours;

    @Size(max = 500, message = "Labels string must be less than 500 characters")
    private String labels;

    private Long assigneeId;

    private Long sprintId;
}
