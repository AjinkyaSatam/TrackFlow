package com.trackflow.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request payload for reassigning an issue assignee.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateIssueAssigneeRequest {

    private Long assigneeId; // Nullable to allow unassigning
}
