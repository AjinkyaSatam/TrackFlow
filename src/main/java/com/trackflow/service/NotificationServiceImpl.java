package com.trackflow.service;

import com.trackflow.dto.NotificationDTO;
import com.trackflow.entity.Notification;
import com.trackflow.entity.NotificationType;
import com.trackflow.entity.User;
import com.trackflow.mapper.NotificationMapper;
import com.trackflow.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service implementation providing User Notification management.
 */
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;

    @Override
    @Transactional
    public void sendNotification(String title, String message, NotificationType type, Long referenceId,
                                 String referenceType, User recipient, User triggeredBy) {
        Notification notification = Notification.builder()
                .title(title)
                .message(message)
                .type(type)
                .referenceId(referenceId)
                .referenceType(referenceType)
                .recipient(recipient)
                .triggeredBy(triggeredBy)
                .read(false)
                .build();
        notificationRepository.save(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationDTO> getUserNotifications(Long userId, Pageable pageable) {
        return notificationRepository.findByRecipientIdAndIsDeletedFalse(userId, pageable)
                .map(notificationMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public long countUnreadNotifications(Long userId) {
        return notificationRepository.countByRecipientIdAndReadFalseAndIsDeletedFalse(userId);
    }

    @Override
    @Transactional
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsRead(userId);
    }
}
