package com.trackflow.controller;

import com.trackflow.dto.*;
import com.trackflow.entity.IssuePriority;
import com.trackflow.entity.IssueStatus;
import com.trackflow.entity.User;
import com.trackflow.exception.AccessDeniedException;
import com.trackflow.security.CustomUserDetails;
import com.trackflow.service.IssueService;
import com.trackflow.service.ProjectService;
import com.trackflow.util.AppConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller managing issue workflow, backlog tasks, and assignments.
 */
@RestController
@RequestMapping("/projects/{projectId}/issues")
@RequiredArgsConstructor
@Tag(name = "Issues", description = "Endpoints for managing workspace issues and workflow updates")
@SecurityRequirement(name = "Bearer Authentication")
public class IssueController {

    private final IssueService issueService;
    private final ProjectService projectService;

    @PostMapping
    @Operation(summary = "Create an issue", description = "Creates a new issue inside a project backlog.")
    public ResponseEntity<ApiResponse<IssueDTO>> createIssue(
            @PathVariable Long projectId,
            @Valid @RequestBody CreateIssueRequest request,
            @AuthenticationPrincipal CustomUserDetails loggedInUser
    ) {
        validateProjectAccess(loggedInUser.getUser(), projectId);
        IssueDTO response = issueService.createIssue(projectId, request, loggedInUser.getUser());
        return ResponseEntity.status(201).body(
                ApiResponse.created(response, "Issue created successfully")
        );
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get issue by ID", description = "Retrieves an issue's details.")
    public ResponseEntity<ApiResponse<IssueDTO>> getIssueById(
            @PathVariable Long projectId,
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails loggedInUser
    ) {
        validateProjectAccess(loggedInUser.getUser(), projectId);
        IssueDTO issue = issueService.getIssueById(id);
        return ResponseEntity.ok(
                ApiResponse.success(issue, "Issue details fetched successfully")
        );
    }

    @GetMapping("/key/{issueKey}")
    @Operation(summary = "Get issue by key", description = "Retrieves details of an issue using its unique key (e.g. TF-42).")
    public ResponseEntity<ApiResponse<IssueDTO>> getIssueByKey(
            @PathVariable Long projectId,
            @PathVariable String issueKey,
            @AuthenticationPrincipal CustomUserDetails loggedInUser
    ) {
        validateProjectAccess(loggedInUser.getUser(), projectId);
        IssueDTO issue = issueService.getIssueByKey(issueKey);
        return ResponseEntity.ok(
                ApiResponse.success(issue, "Issue details fetched successfully")
        );
    }

    @GetMapping
    @Operation(summary = "Search and filter issues", description = "Retrieves a paginated list of project issues filtered by status, priority, assignee, or text search.")
    public ResponseEntity<ApiResponse<Page<IssueDTO>>> getIssues(
            @PathVariable Long projectId,
            @RequestParam(value = "status", required = false) IssueStatus status,
            @RequestParam(value = "priority", required = false) IssuePriority priority,
            @RequestParam(value = "assigneeId", required = false) Long assigneeId,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size,
            @RequestParam(value = "sortBy", defaultValue = AppConstants.DEFAULT_SORT_BY) String sortBy,
            @RequestParam(value = "sortDir", defaultValue = AppConstants.DEFAULT_SORT_DIR) String sortDir,
            @AuthenticationPrincipal CustomUserDetails loggedInUser
    ) {
        validateProjectAccess(loggedInUser.getUser(), projectId);

        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<IssueDTO> issues = issueService.filterIssues(projectId, status, priority, assigneeId, keyword, pageable);
        return ResponseEntity.ok(
                ApiResponse.success(issues, "Issues fetched successfully")
        );
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Update issue status", description = "Enforces state workflow transitions (e.g. moving an issue to IN_PROGRESS).")
    public ResponseEntity<ApiResponse<IssueDTO>> updateIssueStatus(
            @PathVariable Long projectId,
            @PathVariable Long id,
            @Valid @RequestBody UpdateIssueStatusRequest request,
            @AuthenticationPrincipal CustomUserDetails loggedInUser
    ) {
        validateProjectAccess(loggedInUser.getUser(), projectId);
        IssueDTO response = issueService.updateIssueStatus(id, request);
        return ResponseEntity.ok(
                ApiResponse.success(response, "Issue status transitioned successfully")
        );
    }

    @PutMapping("/{id}/assignee")
    @Operation(summary = "Update issue assignee", description = "Updates the assignee of an issue.")
    public ResponseEntity<ApiResponse<IssueDTO>> updateIssueAssignee(
            @PathVariable Long projectId,
            @PathVariable Long id,
            @Valid @RequestBody UpdateIssueAssigneeRequest request,
            @AuthenticationPrincipal CustomUserDetails loggedInUser
    ) {
        validateProjectAccess(loggedInUser.getUser(), projectId);
        IssueDTO response = issueService.updateIssueAssignee(id, request);
        return ResponseEntity.ok(
                ApiResponse.success(response, "Issue assignee updated successfully")
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
