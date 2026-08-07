package com.trackflow.dto;

import com.trackflow.entity.SprintStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request payload for transitioning a sprint's status.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateSprintStatusRequest {

    @NotNull(message = "Sprint status is required")
    private SprintStatus status;
}
