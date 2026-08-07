package com.trackflow.controller;

import com.trackflow.dto.ApiResponse;
import com.trackflow.dto.NotificationDTO;
import com.trackflow.security.CustomUserDetails;
import com.trackflow.service.NotificationService;
import com.trackflow.util.AppConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for retrieving notifications and marking alerts as read.
 */
@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Endpoints for managing user alerts and inbox status")
@SecurityRequirement(name = "Bearer Authentication")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @Operation(summary = "Get user notifications", description = "Retrieves a paginated list of all notifications for the current user.")
    public ResponseEntity<ApiResponse<Page<NotificationDTO>>> getNotifications(
            @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size,
            @AuthenticationPrincipal CustomUserDetails loggedInUser
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<NotificationDTO> response = notificationService.getUserNotifications(loggedInUser.getUser().getId(), pageable);
        return ResponseEntity.ok(
                ApiResponse.success(response, "Notifications retrieved successfully")
        );
    }

    @GetMapping("/unread/count")
    @Operation(summary = "Get unread count", description = "Counts unread notifications in user's inbox.")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(
            @AuthenticationPrincipal CustomUserDetails loggedInUser
    ) {
        long count = notificationService.countUnreadNotifications(loggedInUser.getUser().getId());
        return ResponseEntity.ok(
                ApiResponse.success(count, "Unread count retrieved successfully")
        );
    }

    @PutMapping("/mark-all-read")
    @Operation(summary = "Mark all as read", description = "Marks all unread notifications as read in user's inbox.")
    public ResponseEntity<ApiResponse<String>> markAllRead(
            @AuthenticationPrincipal CustomUserDetails loggedInUser
    ) {
        notificationService.markAllAsRead(loggedInUser.getUser().getId());
        return ResponseEntity.ok(
                ApiResponse.success("All notifications marked as read")
        );
    }
}
