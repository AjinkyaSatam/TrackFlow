package com.trackflow.controller;

import com.trackflow.dto.AddProjectMemberRequest;
import com.trackflow.dto.ApiResponse;
import com.trackflow.dto.CreateProjectRequest;
import com.trackflow.dto.ProjectDTO;
import com.trackflow.dto.ProjectMemberDTO;
import com.trackflow.entity.Role;
import com.trackflow.entity.User;
import com.trackflow.exception.AccessDeniedException;
import com.trackflow.exception.BadRequestException;
import com.trackflow.security.CustomUserDetails;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for project planning and workspace membership allocation.
 */
@RestController
@RequestMapping("/projects")
@RequiredArgsConstructor
@Tag(name = "Projects", description = "Endpoints for project planning and membership management")
@SecurityRequirement(name = "Bearer Authentication")
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    @PreAuthorize("hasRole('ROLE_ORG_ADMIN') or hasRole('ROLE_PROJECT_MANAGER') or hasRole('ROLE_SUPER_ADMIN')")
    @Operation(summary = "Create a project", description = "Creates a new project. Access is restricted to Managers, Org Admins, and Super Admins.")
    public ResponseEntity<ApiResponse<ProjectDTO>> createProject(
            @Valid @RequestBody CreateProjectRequest request,
            @AuthenticationPrincipal CustomUserDetails loggedInUser
    ) {
        ProjectDTO response = projectService.createProject(request, loggedInUser.getUser());
        return ResponseEntity.status(201).body(
                ApiResponse.created(response, "Project created successfully")
        );
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get project by ID", description = "Retrieves project details. User must belong to the organization owning the project.")
    public ResponseEntity<ApiResponse<ProjectDTO>> getProjectById(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails loggedInUser
    ) {
        ProjectDTO project = projectService.getProjectById(id);
        validateTenantAccess(loggedInUser.getUser(), project.getOrganizationId());
        return ResponseEntity.ok(
                ApiResponse.success(project, "Project details fetched successfully")
        );
    }

    @GetMapping
    @Operation(summary = "Get organization projects", description = "Retrieves all projects belonging to the logged-in user's organization, paginated.")
    public ResponseEntity<ApiResponse<Page<ProjectDTO>>> getProjects(
            @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size,
            @RequestParam(value = "sortBy", defaultValue = AppConstants.DEFAULT_SORT_BY) String sortBy,
            @RequestParam(value = "sortDir", defaultValue = AppConstants.DEFAULT_SORT_DIR) String sortDir,
            @AuthenticationPrincipal CustomUserDetails loggedInUser
    ) {
        if (loggedInUser.getUser().getOrganization() == null) {
            throw new BadRequestException("User does not belong to any organization");
        }

        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<ProjectDTO> projects = projectService.getProjectsByOrganization(
                loggedInUser.getUser().getOrganization().getId(),
                pageable
        );
        return ResponseEntity.ok(
                ApiResponse.success(projects, "Projects fetched successfully")
        );
    }

    @PostMapping("/{id}/members")
    @PreAuthorize("hasRole('ROLE_ORG_ADMIN') or hasRole('ROLE_PROJECT_MANAGER') or hasRole('ROLE_SUPER_ADMIN')")
    @Operation(summary = "Add project member", description = "Adds a user to a project's workspace directory.")
    public ResponseEntity<ApiResponse<ProjectMemberDTO>> addMember(
            @PathVariable Long id,
            @Valid @RequestBody AddProjectMemberRequest request,
            @AuthenticationPrincipal CustomUserDetails loggedInUser
    ) {
        ProjectDTO project = projectService.getProjectById(id);
        validateTenantAccess(loggedInUser.getUser(), project.getOrganizationId());
        
        ProjectMemberDTO member = projectService.addMember(id, request);
        return ResponseEntity.ok(
                ApiResponse.success(member, "Member added to project successfully")
        );
    }

    @DeleteMapping("/{id}/members/{userId}")
    @PreAuthorize("hasRole('ROLE_ORG_ADMIN') or hasRole('ROLE_PROJECT_MANAGER') or hasRole('ROLE_SUPER_ADMIN')")
    @Operation(summary = "Remove project member", description = "Removes a user from a project workspace.")
    public ResponseEntity<ApiResponse<String>> removeMember(
            @PathVariable Long id,
            @PathVariable Long userId,
            @AuthenticationPrincipal CustomUserDetails loggedInUser
    ) {
        ProjectDTO project = projectService.getProjectById(id);
        validateTenantAccess(loggedInUser.getUser(), project.getOrganizationId());
        
        projectService.removeMember(id, userId);
        return ResponseEntity.ok(
                ApiResponse.success("Member removed from project successfully")
        );
    }

    @GetMapping("/{id}/members")
    @Operation(summary = "Get project members", description = "Lists all members assigned to a project.")
    public ResponseEntity<ApiResponse<List<ProjectMemberDTO>>> getProjectMembers(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails loggedInUser
    ) {
        ProjectDTO project = projectService.getProjectById(id);
        validateTenantAccess(loggedInUser.getUser(), project.getOrganizationId());
        
        List<ProjectMemberDTO> members = projectService.getProjectMembers(id);
        return ResponseEntity.ok(
                ApiResponse.success(members, "Project members fetched successfully")
        );
    }

    // -----------------------------------------------------------------
    // Access Validations
    // -----------------------------------------------------------------

    private void validateTenantAccess(User loggedInUser, Long targetOrgId) {
        if (loggedInUser.getRole() == Role.SUPER_ADMIN) {
            return;
        }
        if (loggedInUser.getOrganization() == null || !loggedInUser.getOrganization().getId().equals(targetOrgId)) {
            throw new AccessDeniedException("Access Denied: You do not belong to this organization");
        }
    }
}
