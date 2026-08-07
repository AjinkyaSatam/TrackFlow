package com.trackflow.controller;

import com.trackflow.dto.ApiResponse;
import com.trackflow.dto.CommentDTO;
import com.trackflow.dto.CreateCommentRequest;
import com.trackflow.entity.User;
import com.trackflow.exception.AccessDeniedException;
import com.trackflow.security.CustomUserDetails;
import com.trackflow.service.CommentService;
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
 * REST controller for adding collaborative comments and remarks to issues.
 */
@RestController
@RequestMapping("/projects/{projectId}/issues/{issueId}/comments")
@RequiredArgsConstructor
@Tag(name = "Comments", description = "Endpoints for managing issue comments and collaboration")
@SecurityRequirement(name = "Bearer Authentication")
public class CommentController {

    private final CommentService commentService;
    private final ProjectService projectService;

    @PostMapping
    @Operation(summary = "Add a comment", description = "Adds a comment to an issue.")
    public ResponseEntity<ApiResponse<CommentDTO>> addComment(
            @PathVariable Long projectId,
            @PathVariable Long issueId,
            @Valid @RequestBody CreateCommentRequest request,
            @AuthenticationPrincipal CustomUserDetails loggedInUser
    ) {
        validateProjectAccess(loggedInUser.getUser(), projectId);
        CommentDTO comment = commentService.addComment(issueId, request, loggedInUser.getUser());
        return ResponseEntity.status(201).body(
                ApiResponse.created(comment, "Comment added successfully")
        );
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a comment", description = "Updates an existing comment. Users can only edit their own comments.")
    public ResponseEntity<ApiResponse<CommentDTO>> updateComment(
            @PathVariable Long projectId,
            @PathVariable Long id,
            @Valid @RequestBody CreateCommentRequest request,
            @AuthenticationPrincipal CustomUserDetails loggedInUser
    ) {
        validateProjectAccess(loggedInUser.getUser(), projectId);
        CommentDTO comment = commentService.updateComment(id, request, loggedInUser.getUser());
        return ResponseEntity.ok(
                ApiResponse.success(comment, "Comment updated successfully")
        );
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a comment", description = "Deletes a comment. Restricted to the author or Organization Administrators.")
    public ResponseEntity<ApiResponse<String>> deleteComment(
            @PathVariable Long projectId,
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails loggedInUser
    ) {
        validateProjectAccess(loggedInUser.getUser(), projectId);
        commentService.deleteComment(id, loggedInUser.getUser());
        return ResponseEntity.ok(
                ApiResponse.success("Comment deleted successfully")
        );
    }

    @GetMapping
    @Operation(summary = "Get issue comments", description = "Lists comments on an issue, paginated.")
    public ResponseEntity<ApiResponse<Page<CommentDTO>>> getComments(
            @PathVariable Long projectId,
            @PathVariable Long issueId,
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

        Page<CommentDTO> comments = commentService.getCommentsByIssue(issueId, pageable);
        return ResponseEntity.ok(
                ApiResponse.success(comments, "Comments fetched successfully")
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
