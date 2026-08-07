package com.trackflow.mapper;

import com.trackflow.dto.NotificationDTO;
import com.trackflow.entity.Notification;
import org.springframework.stereotype.Component;

/**
 * Mapper utility to translate between Notification entity and NotificationDTO.
 */
@Component
public class NotificationMapper {

    public NotificationDTO toDTO(Notification notification) {
        if (notification == null) {
            return null;
        }

        String triggeredByName = "System";
        if (notification.getTriggeredBy() != null) {
            triggeredByName = notification.getTriggeredBy().getFullName();
        }

        return NotificationDTO.builder()
                .id(notification.getId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .type(notification.getType().name())
                .read(notification.isRead())
                .referenceId(notification.getReferenceId())
                .referenceType(notification.getReferenceType())
                .recipientId(notification.getRecipient() != null ? notification.getRecipient().getId() : null)
                .triggeredByName(triggeredByName)
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
