package com.trackflow.service;

import com.trackflow.dto.ChangePasswordRequest;
import com.trackflow.dto.UpdateUserRequest;
import com.trackflow.dto.UserDTO;
import com.trackflow.entity.User;
import com.trackflow.exception.BadRequestException;
import com.trackflow.exception.ResourceNotFoundException;
import com.trackflow.mapper.UserMapper;
import com.trackflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service implementation providing business logic for User Management.
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public UserDTO getUserById(Long id) {
        User user = findUserOrThrow(id);
        return userMapper.toDTO(user);
    }

    @Override
    @Transactional
    public UserDTO updateUser(Long id, UpdateUserRequest request) {
        User user = findUserOrThrow(id);
        
        user.setFullName(request.getFullName());
        if (request.getProfileImage() != null) {
            user.setProfileImage(request.getProfileImage());
        }

        User updatedUser = userRepository.save(user);
        return userMapper.toDTO(updatedUser);
    }

    @Override
    @Transactional
    public void changePassword(Long id, ChangePasswordRequest request) {
        User user = findUserOrThrow(id);

        // Verify old password
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BadRequestException("Incorrect old password");
        }

        // Set and encrypt the new password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserDTO> searchUsers(Long orgId, String query, Pageable pageable) {
        // Prevent cross-tenant data leaks by enforcing searches only within the organization context
        return userRepository.searchUsersInOrganization(orgId, query, pageable)
                .map(userMapper::toDTO);
    }

    @Override
    @Transactional
    public void deactivateUser(Long id) {
        User user = findUserOrThrow(id);
        user.setActive(false);
        userRepository.save(user);
    }

    private User findUserOrThrow(Long id) {
        return userRepository.findById(id)
                .filter(user -> !user.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
    }
}
