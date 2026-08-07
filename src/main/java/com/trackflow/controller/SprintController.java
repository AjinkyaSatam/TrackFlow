package com.trackflow.controller;

import com.trackflow.dto.ApiResponse;
import com.trackflow.dto.CreateSprintRequest;
import com.trackflow.dto.SprintDTO;
import com.trackflow.dto.UpdateSprintStatusRequest;
import com.trackflow.entity.User;
import com.trackflow.exception.AccessDeniedException;
import com.trackflow.security.CustomUserDetails;
import com.trackflow.service.ProjectService;
import com.trackflow.service.SprintService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for planning iterations and starting active sprints.
 */
@RestController
@RequestMapping("/projects/{projectId}/sprints")
@RequiredArgsConstructor
@Tag(name = "Sprints", description = "Endpoints for sprint planning and lifecycle transitions")
@SecurityRequirement(name = "Bearer Authentication")
public class SprintController {

    private final SprintService sprintService;
    private final ProjectService projectService;

    @PostMapping
    @PreAuthorize("hasRole('ROLE_ORG_ADMIN') or hasRole('ROLE_PROJECT_MANAGER') or hasRole('ROLE_SUPER_ADMIN')")
    @Operation(summary = "Create a sprint", description = "Creates a new sprint. Restricted to Managers and Org Admins.")
    public ResponseEntity<ApiResponse<SprintDTO>> createSprint(
            @PathVariable Long projectId,
            @Valid @RequestBody CreateSprintRequest request,
            @AuthenticationPrincipal CustomUserDetails loggedInUser
    ) {
        validateProjectAccess(loggedInUser.getUser(), projectId);
        SprintDTO response = sprintService.createSprint(projectId, request);
        return ResponseEntity.status(201).body(
                ApiResponse.created(response, "Sprint created successfully")
        );
    }

    @GetMapping
    @Operation(summary = "Get project sprints", description = "Lists all sprints assigned to a project.")
    public ResponseEntity<ApiResponse<List<SprintDTO>>> getSprints(
            @PathVariable Long projectId,
            @AuthenticationPrincipal CustomUserDetails loggedInUser
    ) {
        validateProjectAccess(loggedInUser.getUser(), projectId);
        List<SprintDTO> sprints = sprintService.getSprintsByProject(projectId);
        return ResponseEntity.ok(
                ApiResponse.success(sprints, "Sprints fetched successfully")
        );
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ROLE_ORG_ADMIN') or hasRole('ROLE_PROJECT_MANAGER') or hasRole('ROLE_SUPER_ADMIN')")
    @Operation(summary = "Update sprint status", description = "Transitions a sprint's lifecycle state (e.g. starting a sprint).")
    public ResponseEntity<ApiResponse<SprintDTO>> updateSprintStatus(
            @PathVariable Long projectId,
            @PathVariable Long id,
            @Valid @RequestBody UpdateSprintStatusRequest request,
            @AuthenticationPrincipal CustomUserDetails loggedInUser
    ) {
        validateProjectAccess(loggedInUser.getUser(), projectId);
        SprintDTO response = sprintService.updateSprintStatus(id, request);
        return ResponseEntity.ok(
                ApiResponse.success(response, "Sprint status updated successfully")
        );
    }

    // -----------------------------------------------------------------
    // Access Validations
    // -----------------------------------------------------------------

    private void validateProjectAccess(User loggedInUser, Long projectId) {
        if (loggedInUser.getRole() == com.trackflow.entity.Role.SUPER_ADMIN) {
            return;
        }
        // Load target project to verify org boundary
        var project = projectService.getProjectById(projectId);
        if (loggedInUser.getOrganization() == null || !loggedInUser.getOrganization().getId().equals(project.getOrganizationId())) {
            throw new AccessDeniedException("Access Denied: You do not belong to this organization");
        }
    }
}
