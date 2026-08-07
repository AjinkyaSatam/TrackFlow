package com.trackflow.service;

import com.trackflow.dto.ChangePasswordRequest;
import com.trackflow.dto.UpdateUserRequest;
import com.trackflow.dto.UserDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service interface defining user management operations.
 */
public interface UserService {

    /**
     * Retrieves a user by their unique database ID.
     */
    UserDTO getUserById(Long id);

    /**
     * Updates profile details of the user.
     */
    UserDTO updateUser(Long id, UpdateUserRequest request);

    /**
     * Changes a user's password after verifying the old password.
     */
    void changePassword(Long id, ChangePasswordRequest request);

    /**
     * Performs a paginated search for users belonging to the same organization.
     */
    Page<UserDTO> searchUsers(Long orgId, String query, Pageable pageable);

    /**
     * Deactivates user accounts (admin function).
     */
    void deactivateUser(Long id);
}
