package com.trackflow.controller;

import com.trackflow.dto.ApiResponse;
import com.trackflow.dto.OrganizationDTO;
import com.trackflow.dto.UpdateOrganizationRequest;
import com.trackflow.entity.Role;
import com.trackflow.exception.AccessDeniedException;
import com.trackflow.security.CustomUserDetails;
import com.trackflow.service.OrganizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller exposing organization workspace management endpoints.
 */
@RestController
@RequestMapping("/organizations")
@RequiredArgsConstructor
@Tag(name = "Organizations", description = "Endpoints for managing workspace profile settings")
@SecurityRequirement(name = "Bearer Authentication")
public class OrganizationController {

    private final OrganizationService organizationService;

    @GetMapping("/{id}")
    @Operation(summary = "Get organization by ID", description = "Retrieves details of an organization profile. Access is restricted to members of this organization.")
    public ResponseEntity<ApiResponse<OrganizationDTO>> getOrganizationById(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails loggedInUser
    ) {
        validateTenantAccess(loggedInUser, id);
        OrganizationDTO organizationDTO = organizationService.getOrganizationById(id);
        return ResponseEntity.ok(
                ApiResponse.success(organizationDTO, "Organization details fetched successfully")
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ORG_ADMIN') or hasRole('ROLE_SUPER_ADMIN')")
    @Operation(summary = "Update organization settings", description = "Updates settings for an organization workspace. Restricted to Organization Admin and Super Admin roles.")
    public ResponseEntity<ApiResponse<OrganizationDTO>> updateOrganization(
            @PathVariable Long id,
            @Valid @RequestBody UpdateOrganizationRequest request,
            @AuthenticationPrincipal CustomUserDetails loggedInUser
    ) {
        validateTenantAccess(loggedInUser, id);
        OrganizationDTO organizationDTO = organizationService.updateOrganization(id, request);
        return ResponseEntity.ok(
                ApiResponse.success(organizationDTO, "Organization settings updated successfully")
        );
    }

    // -----------------------------------------------------------------
    // Helper Tenant Access Validations
    // -----------------------------------------------------------------

    /**
     * Enforces organization boundary. Users can only view or edit their own organization profile.
     */
    private void validateTenantAccess(CustomUserDetails loggedInUser, Long targetOrgId) {
        // Super Admins can access any organization
        if (loggedInUser.getUser().getRole() == Role.SUPER_ADMIN) {
            return;
        }

        // Verify that user belongs to the requested organization
        if (loggedInUser.getUser().getOrganization() == null ||
            !loggedInUser.getUser().getOrganization().getId().equals(targetOrgId)) {
            throw new AccessDeniedException("Access Denied: You do not belong to this organization");
        }
    }
}
