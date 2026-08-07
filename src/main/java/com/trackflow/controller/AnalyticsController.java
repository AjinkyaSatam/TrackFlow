package com.trackflow.controller;

import com.trackflow.dto.ApiResponse;
import com.trackflow.dto.SprintHealthDTO;
import com.trackflow.dto.WorkloadSuggestionDTO;
import com.trackflow.entity.User;
import com.trackflow.exception.AccessDeniedException;
import com.trackflow.security.CustomUserDetails;
import com.trackflow.service.AnalyticsService;
import com.trackflow.service.ProjectService;
import com.trackflow.service.SprintService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for retrieving intelligent developer metrics and sprint health scores.
 */
@RestController
@RequestMapping("/projects/{projectId}/analytics")
@RequiredArgsConstructor
@Tag(name = "Analytics", description = "Endpoints for intelligent sprint health and developer workloads balancing")
@SecurityRequirement(name = "Bearer Authentication")
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final ProjectService projectService;
    private final SprintService sprintService;

    @GetMapping("/workload-suggestions")
    @Operation(summary = "Get workload suggestions", description = "Lists developers sorted by current tasks workload, aiding task allocation planning.")
    public ResponseEntity<ApiResponse<List<WorkloadSuggestionDTO>>> getWorkloadSuggestions(
            @PathVariable Long projectId,
            @AuthenticationPrincipal CustomUserDetails loggedInUser
    ) {
        validateProjectAccess(loggedInUser.getUser(), projectId);
        List<WorkloadSuggestionDTO> suggestions = analyticsService.getWorkloadSuggestions(projectId);
        return ResponseEntity.ok(
                ApiResponse.success(suggestions, "Workload suggestions retrieved successfully")
        );
    }

    @GetMapping("/sprints/{sprintId}/health")
    @Operation(summary = "Get sprint health score", description = "Computes a health score (0-100) based on critical bugs, completion rates, and balance.")
    public ResponseEntity<ApiResponse<SprintHealthDTO>> getSprintHealth(
            @PathVariable Long projectId,
            @PathVariable Long sprintId,
            @AuthenticationPrincipal CustomUserDetails loggedInUser
    ) {
        validateProjectAccess(loggedInUser.getUser(), projectId);
        SprintHealthDTO health = analyticsService.getSprintHealth(sprintId);
        return ResponseEntity.ok(
                ApiResponse.success(health, "Sprint health score computed successfully")
        );
    }

    // -----------------------------------------------------------------
    // Access Validations
    // -----------------------------------------------------------------

    private void validateProjectAccess(User loggedInUser, Long projectId) {
        if (loggedInUser.getRole() == com.trackflow.entity.Role.SUPER_ADMIN) {
            return;
        }
        var project = projectService.getProjectById(projectId);
        if (loggedInUser.getOrganization() == null || !loggedInUser.getOrganization().getId().equals(project.getOrganizationId())) {
            throw new AccessDeniedException("Access Denied: You do not belong to this organization");
        }
    }
}
