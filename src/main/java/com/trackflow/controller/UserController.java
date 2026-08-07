package com.trackflow.controller;

import com.trackflow.dto.ApiResponse;
import com.trackflow.dto.ChangePasswordRequest;
import com.trackflow.dto.UpdateUserRequest;
import com.trackflow.dto.UserDTO;
import com.trackflow.entity.Role;
import com.trackflow.entity.User;
import com.trackflow.exception.AccessDeniedException;
import com.trackflow.exception.BadRequestException;
import com.trackflow.security.CustomUserDetails;
import com.trackflow.service.UserService;
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

/**
 * REST controller managing profile configurations and organizational user directories.
 */
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Endpoints for managing user profiles and team directories")
@SecurityRequirement(name = "Bearer Authentication")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @Operation(summary = "Get current user profile", description = "Retrieves the profile of the currently logged-in user.")
    public ResponseEntity<ApiResponse<UserDTO>> getCurrentUser(@AuthenticationPrincipal CustomUserDetails userDetails) {
        UserDTO userDTO = userService.getUserById(userDetails.getUser().getId());
        return ResponseEntity.ok(
                ApiResponse.success(userDTO, "Current user profile fetched successfully")
        );
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID", description = "Retrieves a user profile by their database ID.")
    public ResponseEntity<ApiResponse<UserDTO>> getUserById(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails loggedInUser
    ) {
        validateTenantAccess(loggedInUser.getUser(), id);
        UserDTO userDTO = userService.getUserById(id);
        return ResponseEntity.ok(
                ApiResponse.success(userDTO, "User profile fetched successfully")
        );
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update user profile", description = "Updates details for a specific user. Users can only update their own profile.")
    public ResponseEntity<ApiResponse<UserDTO>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request,
            @AuthenticationPrincipal CustomUserDetails loggedInUser
    ) {
        validateSelfAccess(loggedInUser.getUser(), id, "update profile of");
        UserDTO userDTO = userService.updateUser(id, request);
        return ResponseEntity.ok(
                ApiResponse.success(userDTO, "Profile updated successfully")
        );
    }

    @PutMapping("/{id}/change-password")
    @Operation(summary = "Change password", description = "Changes a user's password. Users can only modify their own credentials.")
    public ResponseEntity<ApiResponse<String>> changePassword(
            @PathVariable Long id,
            @Valid @RequestBody ChangePasswordRequest request,
            @AuthenticationPrincipal CustomUserDetails loggedInUser
    ) {
        validateSelfAccess(loggedInUser.getUser(), id, "change password of");
        userService.changePassword(id, request);
        return ResponseEntity.ok(
                ApiResponse.success("Password changed successfully")
        );
    }

    @GetMapping
    @Operation(summary = "Search users in organization", description = "Searches for active team members within the logged-in user's organization.")
    public ResponseEntity<ApiResponse<Page<UserDTO>>> searchUsers(
            @RequestParam(value = "query", defaultValue = "") String query,
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

        Page<UserDTO> users = userService.searchUsers(
                loggedInUser.getUser().getOrganization().getId(),
                query,
                pageable
        );
        return ResponseEntity.ok(
                ApiResponse.success(users, "User search results fetched successfully")
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ORG_ADMIN') or hasRole('ROLE_SUPER_ADMIN')")
    @Operation(summary = "Deactivate user", description = "Deactivates a user's account. Restricted to Organization Admin and Super Admin roles.")
    public ResponseEntity<ApiResponse<String>> deactivateUser(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails loggedInUser
    ) {
        validateTenantAccess(loggedInUser.getUser(), id);
        userService.deactivateUser(id);
        return ResponseEntity.ok(
                ApiResponse.success("User account deactivated successfully")
        );
    }

    // -----------------------------------------------------------------
    // Helper Authorization Validations
    // -----------------------------------------------------------------

    /**
     * Verifies that the targeted action is acting on the user's OWN account.
     */
    private void validateSelfAccess(User loggedInUser, Long targetUserId, String action) {
        if (!loggedInUser.getId().equals(targetUserId)) {
            throw new AccessDeniedException(String.format("You do not have permission to %s this user", action));
        }
    }

    /**
     * Verifies that the targeted user belongs to the SAME organization.
     * Prevents cross-tenant leaks.
     */
    private void validateTenantAccess(User loggedInUser, Long targetUserId) {
        // Super Admins can bypass cross-tenant validation rules
        if (loggedInUser.getRole() == Role.SUPER_ADMIN) {
            return;
        }

        UserDTO targetUser = userService.getUserById(targetUserId);
        if (loggedInUser.getOrganization() == null || targetUser.getOrganizationId() == null ||
            !loggedInUser.getOrganization().getId().equals(targetUser.getOrganizationId())) {
            throw new AccessDeniedException("Access Denied: Target user belongs to a different organization");
        }
    }
}
