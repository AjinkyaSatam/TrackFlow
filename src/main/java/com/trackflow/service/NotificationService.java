package com.trackflow.service;

import com.trackflow.dto.NotificationDTO;
import com.trackflow.entity.NotificationType;
import com.trackflow.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service interface defining user notification operations.
 */
public interface NotificationService {

    void sendNotification(String title, String message, NotificationType type, Long referenceId, String referenceType, User recipient, User triggeredBy);

    Page<NotificationDTO> getUserNotifications(Long userId, Pageable pageable);

    long countUnreadNotifications(Long userId);

    void markAllAsRead(Long userId);
}
