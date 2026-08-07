package com.trackflow.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Data Transfer Object representing a user notification.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {

    private Long id;
    private String title;
    private String message;
    private String type;
    private boolean read;
    private Long referenceId;
    private String referenceType;
    private Long recipientId;
    private String triggeredByName;
    private LocalDateTime createdAt;
}
