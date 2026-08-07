package com.trackflow.dto;

import com.trackflow.entity.IssueStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request payload for transitioning an issue's workflow status.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateIssueStatusRequest {

    @NotNull(message = "Issue status is required")
    private IssueStatus status;
}
